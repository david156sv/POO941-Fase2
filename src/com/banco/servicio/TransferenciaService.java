package com.banco.servicio;

import com.banco.excepcion.CuentaNoEncontradaException;
import com.banco.excepcion.EstadoCuentaInvalidoException;
import com.banco.excepcion.FondosInsuficientesException;
import com.banco.excepcion.MontoInvalidoException;
import com.banco.excepcion.OperacionBancariaException;
import com.banco.util.ConexionDB;
import com.banco.util.dao.MovimientoDAO;
import com.banco.util.modelo.EstadoCuenta;
import com.banco.util.modelo.Movimiento;
import com.banco.util.modelo.TipoMovimiento;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Transferencias entre cuentas (Persona 2).
 *
 * Reglas (PDF Fase 2): cuenta origen y destino existen, ambas Activas, monto > 0,
 * saldo suficiente en origen, se descuenta del origen y se suma al destino.
 * Todo ocurre en UNA transaccion: o se hace completo (commit) o no se hace nada (rollback).
 */
public class TransferenciaService {

    // Si el equipo adopta Log4J, solo cambian estas dos lineas (import + declaracion).
    private static final Logger LOG = Logger.getLogger(TransferenciaService.class.getName());

    private static final int MAX_DESCRIPCION = 200;   // largo de movimiento.descripcion

    private final MovimientoDAO movimientoDao = new MovimientoDAO();

    /** Datos minimos de una cuenta mientras dura la transaccion. */
    private static class CuentaBloqueada {
        int id;
        String numero;
        BigDecimal saldo;
        EstadoCuenta estado;
    }

    private static final String SQL_CUENTAS_BLOQUEO =
            "SELECT id_cuenta, numero_cuenta, saldo, estado FROM cuenta_bancaria "
          + "WHERE numero_cuenta IN (?, ?) ORDER BY numero_cuenta FOR UPDATE";
    private static final String SQL_ACTUALIZAR_SALDO =
            "UPDATE cuenta_bancaria SET saldo = ? WHERE id_cuenta = ?";

    /**
     * Transfiere {@code monto} de la cuenta origen a la cuenta destino.
     *
     * @param descripcion motivo opcional (puede ser null o vacio)
     * @return el movimiento registrado en la cuenta ORIGEN (con su saldo resultante)
     */
    public Movimiento transferir(String numeroOrigen, String numeroDestino,
                                 BigDecimal monto, String descripcion)
            throws OperacionBancariaException, SQLException {

        // 1) Validaciones que no necesitan la base de datos
        numeroOrigen = limpiar(numeroOrigen);
        numeroDestino = limpiar(numeroDestino);
        if (numeroOrigen.isEmpty() || numeroDestino.isEmpty()) {
            throw new OperacionBancariaException("Debe indicar la cuenta origen y la cuenta destino.");
        }
        if (numeroOrigen.equals(numeroDestino)) {
            throw new OperacionBancariaException("La cuenta origen y la cuenta destino no pueden ser la misma.");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MontoInvalidoException(monto);
        }
        if (monto.stripTrailingZeros().scale() > 2) {
            throw new OperacionBancariaException("El monto admite como maximo 2 decimales.");
        }

        LOG.log(Level.INFO, "Solicitud de transferencia {0} -> {1} por {2}",
                new Object[]{numeroOrigen, numeroDestino, monto});

        // 2) Transaccion
        Connection con = ConexionDB.getConexion();
        try {
            con.setAutoCommit(false);                                   // inicia la transaccion

            Map<String, CuentaBloqueada> cuentas = bloquearCuentas(con, numeroOrigen, numeroDestino);
            CuentaBloqueada origen = cuentas.get(numeroOrigen);
            CuentaBloqueada destino = cuentas.get(numeroDestino);

            if (origen == null)  throw new CuentaNoEncontradaException(numeroOrigen);
            if (destino == null) throw new CuentaNoEncontradaException(numeroDestino);
            if (origen.estado != EstadoCuenta.ACTIVA) {
                throw new EstadoCuentaInvalidoException(numeroOrigen, origen.estado);
            }
            if (destino.estado != EstadoCuenta.ACTIVA) {
                throw new EstadoCuentaInvalidoException(numeroDestino, destino.estado);
            }
            if (origen.saldo.compareTo(monto) < 0) {
                throw new FondosInsuficientesException(origen.saldo, monto);
            }

            BigDecimal saldoOrigen = origen.saldo.subtract(monto);       // debito
            BigDecimal saldoDestino = destino.saldo.add(monto);          // abono
            actualizarSaldo(con, origen.id, saldoOrigen);
            actualizarSaldo(con, destino.id, saldoDestino);

            Movimiento movOrigen = new Movimiento(origen.id, TipoMovimiento.TRANSFERENCIA, monto,
                    armarDescripcion("Transferencia enviada a " + numeroDestino, descripcion), saldoOrigen);
            Movimiento movDestino = new Movimiento(destino.id, TipoMovimiento.TRANSFERENCIA, monto,
                    armarDescripcion("Transferencia recibida de " + numeroOrigen, descripcion), saldoDestino);
            movimientoDao.insertar(con, movOrigen);
            movimientoDao.insertar(con, movDestino);

            con.commit();                                                // todo salio bien
            LOG.log(Level.INFO, "Transferencia exitosa {0} -> {1} por {2}",
                    new Object[]{numeroOrigen, numeroDestino, monto});
            return movOrigen;

        } catch (SQLException | OperacionBancariaException | RuntimeException ex) {
            try {
                con.rollback();                                          // algo fallo: no se cambia nada
                LOG.info("Rollback ejecutado: la transferencia no se aplico.");
            } catch (SQLException r) {
                LOG.log(Level.SEVERE, "Fallo el rollback", r);
            }
            if (ex instanceof OperacionBancariaException) {
                LOG.log(Level.WARNING, "Transferencia rechazada: {0}", ex.getMessage());
            } else {
                LOG.log(Level.SEVERE, "Error inesperado en transferencia", ex);
            }
            throw ex;
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException e) { /* ignorar */ }
            ConexionDB.cerrar(con);
        }
    }

    /** Bloquea ambas filas (FOR UPDATE) en orden fijo para evitar bloqueos cruzados. */
    private Map<String, CuentaBloqueada> bloquearCuentas(Connection con, String a, String b)
            throws SQLException {
        Map<String, CuentaBloqueada> mapa = new HashMap<>();
        try (PreparedStatement ps = con.prepareStatement(SQL_CUENTAS_BLOQUEO)) {
            ps.setString(1, a);
            ps.setString(2, b);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CuentaBloqueada c = new CuentaBloqueada();
                    c.id = rs.getInt("id_cuenta");
                    c.numero = rs.getString("numero_cuenta");
                    c.saldo = rs.getBigDecimal("saldo");
                    c.estado = EstadoCuenta.desdeEtiqueta(rs.getString("estado"));
                    mapa.put(c.numero, c);
                }
            }
        }
        return mapa;
    }

    private void actualizarSaldo(Connection con, int idCuenta, BigDecimal nuevoSaldo)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR_SALDO)) {
            ps.setBigDecimal(1, nuevoSaldo);
            ps.setInt(2, idCuenta);
            ps.executeUpdate();
        }
    }

    private String armarDescripcion(String base, String motivo) {
        String texto = (motivo == null || motivo.trim().isEmpty()) ? base : base + " - " + motivo.trim();
        return texto.length() > MAX_DESCRIPCION ? texto.substring(0, MAX_DESCRIPCION) : texto;
    }

    private String limpiar(String s) {
        return s == null ? "" : s.trim();
    }
}

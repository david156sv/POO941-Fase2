package com.banco.util.dao;

import com.banco.util.ConexionDB;
import com.banco.util.modelo.Movimiento;
import com.banco.util.modelo.TipoMovimiento;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Consultas de movimientos con filtros (Persona 2).
 * Es una clase aparte para no tocar MovimientoDAO (Persona 1) y evitar conflictos en Git.
 */
public class ConsultaMovimientoDAO {

    private static final int LIMITE_FILAS = 1000;

    private static final String SQL_BASE =
            "SELECT m.id_movimiento, m.id_cuenta, c.numero_cuenta, m.tipo_movimiento, m.monto, "
          + "m.fecha_hora, m.descripcion, m.saldo_resultante "
          + "FROM movimiento m INNER JOIN cuenta_bancaria c ON c.id_cuenta = m.id_cuenta ";

    /**
     * Historial con filtros opcionales: cualquier parametro en null/vacio no filtra.
     *
     * @param numeroCuenta numero de cuenta exacto
     * @param tipo         Depósito, Retiro o Transferencia
     * @param desde        fecha inicial (incluida)
     * @param hasta        fecha final (incluida)
     */
    public List<Movimiento> buscar(String numeroCuenta, TipoMovimiento tipo,
                                   LocalDate desde, LocalDate hasta) throws SQLException {
        StringBuilder sql = new StringBuilder(SQL_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (numeroCuenta != null && !numeroCuenta.trim().isEmpty()) {
            sql.append("AND c.numero_cuenta = ? ");
            params.add(numeroCuenta.trim());
        }
        if (tipo != null) {
            sql.append("AND m.tipo_movimiento = ? ");
            params.add(tipo.getEtiqueta());
        }
        if (desde != null) {
            sql.append("AND m.fecha_hora >= ? ");
            params.add(Timestamp.valueOf(desde.atStartOfDay()));
        }
        if (hasta != null) {
            sql.append("AND m.fecha_hora < ? ");                       // hasta es inclusivo
            params.add(Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
        }
        sql.append("ORDER BY m.fecha_hora DESC, m.id_movimiento DESC LIMIT ").append(LIMITE_FILAS);

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Movimiento> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        }
    }

    /** Consulta de un movimiento individual. Devuelve null si no existe. */
    public Movimiento buscarPorId(int idMovimiento) throws SQLException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_BASE + "WHERE m.id_movimiento = ?")) {
            ps.setInt(1, idMovimiento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    private Movimiento mapear(ResultSet rs) throws SQLException {
        Movimiento m = new Movimiento();
        m.setIdMovimiento(rs.getInt("id_movimiento"));
        m.setIdCuenta(rs.getInt("id_cuenta"));
        m.setNumeroCuenta(rs.getString("numero_cuenta"));
        m.setTipoMovimiento(TipoMovimiento.desdeEtiqueta(rs.getString("tipo_movimiento")));
        m.setMonto(rs.getBigDecimal("monto"));
        Timestamp ts = rs.getTimestamp("fecha_hora");
        m.setFechaHora(ts == null ? null : ts.toLocalDateTime());
        m.setDescripcion(rs.getString("descripcion"));
        m.setSaldoResultante(rs.getBigDecimal("saldo_resultante"));
        return m;
    }
}

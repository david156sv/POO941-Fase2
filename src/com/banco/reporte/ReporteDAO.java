package com.banco.reporte;

import com.banco.util.ConexionDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** Consultas de solo lectura. Las fechas finales incluyen el dia completo. */
public class ReporteDAO {
    @FunctionalInterface
    public interface ConexionFactory { Connection abrir() throws SQLException; }
    private final ConexionFactory conexiones;
    public ReporteDAO() { this(ConexionDB::getConexion); }
    public ReporteDAO(ConexionFactory conexiones) { this.conexiones=Objects.requireNonNull(conexiones); }
    public Reporte generar(TipoReporte tipo, String cuenta, String cliente,
                           LocalDate desde, LocalDate hasta) throws SQLException {
        if (tipo == null) throw new IllegalArgumentException("Seleccione un reporte.");
        if (tipo.usaFechas() && desde != null && hasta != null && desde.isAfter(hasta))
            throw new IllegalArgumentException("La fecha inicial debe ser anterior o igual a la final.");
        cuenta = cuenta == null ? "" : cuenta.trim();
        cliente = cliente == null ? "" : cliente.trim();
        String sql;
        boolean movimientos = tipo.usaFechas();
        boolean total = tipo == TipoReporte.DEPOSITADO || tipo == TipoReporte.RETIRADO || tipo == TipoReporte.TRANSFERIDO;
        if (tipo == TipoReporte.CLIENTES) {
            sql = "SELECT cl.codigo_cliente AS Codigo, cl.dui AS DUI, cl.nombre_completo AS Cliente, "
                + "cl.telefono AS Telefono, cl.correo AS Correo, cl.direccion AS Direccion, cl.estado AS Estado FROM cliente cl WHERE 1=1";
        } else if (!movimientos) {
            sql = "SELECT c.numero_cuenta AS Cuenta, cl.codigo_cliente AS Codigo, cl.nombre_completo AS Cliente, "
                + "c.tipo_cuenta AS Tipo, c.saldo AS Saldo, c.fecha_apertura AS Apertura, c.estado AS Estado "
                + "FROM cuenta_bancaria c JOIN cliente cl ON cl.id_cliente=c.id_cliente WHERE c.estado='"
                + (tipo == TipoReporte.CUENTAS_ACTIVAS ? "Activa" : "Bloqueada") + "'";
        } else {
            String campos = total ? "COUNT(*) AS Operaciones, COALESCE(SUM(m.monto),0.00) AS Total"
                : "m.id_movimiento AS ID, c.numero_cuenta AS Cuenta, cl.nombre_completo AS Cliente, "
                + "m.fecha_hora AS Fecha, m.tipo_movimiento AS Tipo, m.monto AS Monto, "
                + "m.saldo_resultante AS Saldo, m.descripcion AS Descripcion";
            sql = "SELECT " + campos + " FROM movimiento m JOIN cuenta_bancaria c ON c.id_cuenta=m.id_cuenta "
                + "JOIN cliente cl ON cl.id_cliente=c.id_cliente WHERE 1=1";
        }
        List<Object> parametros = new ArrayList<>();
        StringBuilder filtro = new StringBuilder();
        if (tipo.usaCuenta() && !cuenta.isEmpty()) {
            sql += " AND c.numero_cuenta=?"; parametros.add(cuenta);
            filtro.append("Cuenta: ").append(cuenta).append("; ");
        }
        if (!cliente.isEmpty()) {
            sql += " AND (LOCATE(?,cl.codigo_cliente)>0 OR LOCATE(?,cl.dui)>0 OR LOCATE(?,cl.nombre_completo)>0)";
            parametros.add(cliente); parametros.add(cliente); parametros.add(cliente);
            filtro.append("Cliente: ").append(cliente).append("; ");
        }
        if (movimientos && desde != null) {
            sql += " AND m.fecha_hora>=?"; parametros.add(desde.atStartOfDay());
            filtro.append("Desde: ").append(desde).append("; ");
        }
        if (movimientos && hasta != null) {
            sql += " AND m.fecha_hora<?"; parametros.add(hasta.plusDays(1).atStartOfDay());
            filtro.append("Hasta: ").append(hasta).append("; ");
        }
        if (total) {
            String movimiento = tipo == TipoReporte.DEPOSITADO ? "Depósito" : tipo == TipoReporte.RETIRADO ? "Retiro" : "Transferencia";
            sql += " AND m.tipo_movimiento=?"; parametros.add(movimiento);
            // El servicio existente registra envio y recepcion con estas descripciones.
            if (tipo == TipoReporte.TRANSFERIDO) {
                sql += " AND m.descripcion LIKE 'Transferencia enviada a %'";
                filtro.append("Solo transferencias enviadas (sin duplicar recepciones); ");
            }
        } else if (movimientos) {
            sql += tipo == TipoReporte.POR_CUENTA ? " ORDER BY c.numero_cuenta,m.fecha_hora,m.id_movimiento"
                                                 : " ORDER BY m.fecha_hora DESC,m.id_movimiento DESC";
        } else sql += tipo == TipoReporte.CLIENTES ? " ORDER BY cl.codigo_cliente" : " ORDER BY c.numero_cuenta";
        List<String> columnas = new ArrayList<>();
        List<List<Object>> filas = new ArrayList<>();
        try (Connection con = conexiones.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i=0; i<parametros.size(); i++) ps.setObject(i+1,parametros.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                for (int i=1; i<=meta.getColumnCount(); i++) columnas.add(meta.getColumnLabel(i));
                while (rs.next()) {
                    List<Object> fila = new ArrayList<>();
                    for (int i=1; i<=columnas.size(); i++) fila.add(meta.getColumnType(i)==Types.TIMESTAMP ? rs.getObject(i,java.time.LocalDateTime.class) : rs.getObject(i));
                    filas.add(fila);
                }
            }
        }
        return new Reporte(tipo.toString(), filtro.length()==0 ? "Sin filtros" : filtro.toString(), columnas, filas);
    }
}

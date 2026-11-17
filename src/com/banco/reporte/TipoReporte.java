package com.banco.reporte;

/** Los ocho reportes requeridos para la persona 3. */
public enum TipoReporte {
    CUENTAS_ACTIVAS("Cuentas activas"), CUENTAS_BLOQUEADAS("Cuentas bloqueadas"),
    CLIENTES("Clientes registrados"), HISTORIAL("Historial de transacciones"),
    POR_CUENTA("Transacciones por cuenta"), DEPOSITADO("Total depositado"),
    RETIRADO("Total retirado"), TRANSFERIDO("Total transferido");
    private final String titulo;
    TipoReporte(String titulo) { this.titulo = titulo; }
    public boolean usaFechas() { return ordinal() >= HISTORIAL.ordinal(); }
    public boolean usaCuenta() { return this != CLIENTES; }
    @Override public String toString() { return titulo; }
}

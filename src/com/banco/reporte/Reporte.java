package com.banco.reporte;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Resultado independiente de la interfaz y de la conexion JDBC. */
public final class Reporte {
    public final String titulo;
    public final String filtros;
    public final List<String> columnas;
    public final List<List<Object>> filas;
    public Reporte(String titulo, String filtros, List<String> columnas, List<List<Object>> filas) {
        this.titulo = titulo;
        this.filtros = filtros;
        this.columnas = Collections.unmodifiableList(new ArrayList<>(columnas));
        List<List<Object>> copia = new ArrayList<>();
        for (List<Object> fila : filas) copia.add(Collections.unmodifiableList(new ArrayList<>(fila)));
        this.filas = Collections.unmodifiableList(copia);
    }
}

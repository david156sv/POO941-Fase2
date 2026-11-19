package com.banco.reporte;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.Normalizer;
import java.util.Locale;

public final class ArchivoReporte {
    private ArchivoReporte() { }
    public static Path generar(Reporte reporte) throws IOException {
        return generar(reporte,Paths.get(System.getProperty("user.home"),"BancoUDB","Reportes"));
    }
    public static Path generar(Reporte reporte, Path carpeta) throws IOException {
        Files.createDirectories(carpeta);
        String nombre=Normalizer.normalize(reporte.titulo,Normalizer.Form.NFD)
            .replaceAll("\\p{M}","").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","-");
        String fecha=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path archivo=Files.createTempFile(carpeta,nombre+"-"+fecha+"-",".pdf");
        try {
            ExportadorPDF.exportar(reporte,archivo);
            return archivo;
        } catch (IOException | RuntimeException ex) {
            try { Files.deleteIfExists(archivo); } catch (IOException limpieza) { ex.addSuppressed(limpieza); }
            throw ex;
        }
    }
}

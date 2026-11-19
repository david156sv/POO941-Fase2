package com.banco.reporte;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ExportadorPDF {
    private ExportadorPDF() { }
    public static void exportar(Reporte reporte, Path destino) throws IOException {
        if (reporte.columnas.isEmpty()) throw new IllegalArgumentException("El reporte no tiene columnas.");
        boolean ancho = reporte.columnas.size() > 4;
        Document documento = new Document(ancho ? PageSize.A4.rotate() : PageSize.A4, 30,30,32,38);
        try (OutputStream salida = Files.newOutputStream(destino)) {
            PdfWriter writer = PdfWriter.getInstance(documento,salida);
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override public void onEndPage(PdfWriter w, Document d) {
                    ColumnText.showTextAligned(w.getDirectContent(),Element.ALIGN_RIGHT,
                        new Phrase("Página " + w.getPageNumber(),fuente(8,Font.NORMAL)),
                        d.right(),d.bottom()-18,0);
                }
            });
            documento.addTitle(reporte.titulo);
            documento.addAuthor("Banco Académico UDB");
            documento.open();
            documento.add(new Paragraph("BANCO ACADÉMICO UDB",fuente(9,Font.BOLD)));
            Paragraph titulo = new Paragraph(reporte.titulo,fuente(18,Font.BOLD));
            titulo.setSpacingAfter(8); documento.add(titulo);
            Paragraph filtros = new Paragraph(reporte.filtros,fuente(9,Font.NORMAL));
            filtros.setSpacingAfter(5); documento.add(filtros);
            Paragraph fecha = new Paragraph("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                + " | Importes en USD | " + reporte.filas.size() + " fila(s)",fuente(8,Font.NORMAL));
            fecha.setSpacingAfter(14); documento.add(fecha);
            PdfPTable tabla = new PdfPTable(reporte.columnas.size());
            tabla.setWidthPercentage(100);
            float[] anchos = new float[reporte.columnas.size()];
            for (int i=0;i<anchos.length;i++) {
                String columna = reporte.columnas.get(i);
                anchos[i] = columna.equals("Descripcion") || columna.equals("Direccion") ? 3.2f
                    : columna.equals("Cliente") || columna.equals("Correo") ? 2.5f
                    : columna.equals("Fecha") ? 1.9f : columna.equals("ID") ? .6f : 1.5f;
                PdfPCell celda = new PdfPCell(new Phrase(columna,fuente(8,Font.BOLD)));
                celda.setBackgroundColor(new Color(226,235,245)); celda.setPadding(6);
                celda.setBorderColor(new Color(185,195,205)); tabla.addCell(celda);
            }
            tabla.setWidths(anchos); tabla.setHeaderRows(1); tabla.setSplitLate(false);
            int fila=0;
            for (java.util.List<Object> valores : reporte.filas) {
                for (Object valor : valores) {
                    String texto=valor==null?"":valor instanceof LocalDateTime
                        ? ((LocalDateTime)valor).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : valor.toString();
                    PdfPCell celda = new PdfPCell(new Phrase(texto,fuente(8,Font.NORMAL)));
                    celda.setPadding(5); celda.setVerticalAlignment(Element.ALIGN_TOP);
                    celda.setBorderColor(new Color(205,212,220));
                    if (valor instanceof BigDecimal || valor instanceof Number) celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    if (fila%2==1) celda.setBackgroundColor(new Color(246,248,251));
                    tabla.addCell(celda);
                }
                fila++;
            }
            documento.add(tabla);
            if (reporte.filas.isEmpty()) documento.add(new Paragraph("Sin resultados para los filtros seleccionados.",fuente(10,Font.NORMAL)));
            documento.close();
        } catch (DocumentException ex) {
            throw new IOException("No se pudo generar el PDF.",ex);
        } finally {
            if (documento.isOpen()) documento.close();
        }
    }
    private static Font fuente(float tamano,int estilo) {
        return FontFactory.getFont(FontFactory.HELVETICA,BaseFont.WINANSI,false,tamano,estilo,Color.BLACK);
    }
}

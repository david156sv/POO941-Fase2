package com.banco.util.vista;

import com.banco.reporte.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ReportesForm extends JFrame {
    private Reporte reporte;

    public ReportesForm() {
        initComponents();
        MenuReportes.instalar(this);
        java.net.URL logo = getClass().getResource("/com/banco/img/logo.jfif");
        if (logo != null) setIconImage(new ImageIcon(logo).getImage());
        tipo.setModel(new DefaultComboBoxModel<>(TipoReporte.values()));
        tabla.getTableHeader().setReorderingAllowed(false);
        javax.swing.event.DocumentListener cambios = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { invalidar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { invalidar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { invalidar(); }
        };
        for (JTextField campo : new JTextField[]{cuenta,cliente,desde,hasta}) campo.getDocument().addDocumentListener(cambios);
        actualizarCampos();
        setSize(1140,650);
        setMinimumSize(new Dimension(950,500));
        setLocationRelativeTo(null);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contenido = new javax.swing.JPanel();
        cabecera = new javax.swing.JPanel();
        titulo = new javax.swing.JLabel();
        campos = new javax.swing.JPanel();
        lblReporte = new javax.swing.JLabel();
        tipo = new javax.swing.JComboBox<>();
        lblCuenta = new javax.swing.JLabel();
        cuenta = new javax.swing.JTextField();
        lblCliente = new javax.swing.JLabel();
        cliente = new javax.swing.JTextField();
        lblDesde = new javax.swing.JLabel();
        desde = new javax.swing.JTextField();
        lblHasta = new javax.swing.JLabel();
        hasta = new javax.swing.JTextField();
        lblAyudaFechas = new javax.swing.JLabel();
        lblEspacio = new javax.swing.JLabel();
        acciones = new javax.swing.JPanel();
        generar = new javax.swing.JButton();
        limpiar = new javax.swing.JButton();
        imprimir = new javax.swing.JButton();
        scrollResultados = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();
        pie = new javax.swing.JPanel();
        filtros = new javax.swing.JLabel();
        resumen = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Banco Académico UDB - Reportes");

        contenido.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        contenido.setLayout(new java.awt.BorderLayout());

        cabecera.setLayout(new java.awt.BorderLayout());

        titulo.setText("Reportes financieros y de cuentas");
        titulo.setFont(new java.awt.Font("SansSerif", 1, 21)); // NOI18N
        cabecera.add(titulo, java.awt.BorderLayout.NORTH);

        campos.setLayout(new java.awt.GridLayout(3, 4));

        lblReporte.setText("Reporte:");
        campos.add(lblReporte);

        tipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tipoActionPerformed(evt);
            }
        });
        campos.add(tipo);

        lblCuenta.setText("Cuenta exacta (opcional):");
        campos.add(lblCuenta);

        cuenta.setColumns(13);
        campos.add(cuenta);

        lblCliente.setText("Cliente: código, DUI o nombre:");
        campos.add(lblCliente);

        cliente.setColumns(17);
        campos.add(cliente);

        lblDesde.setText("Desde (AAAA-MM-DD):");
        campos.add(lblDesde);

        desde.setColumns(10);
        campos.add(desde);

        lblHasta.setText("Hasta (AAAA-MM-DD):");
        campos.add(lblHasta);

        hasta.setColumns(10);
        campos.add(hasta);

        lblAyudaFechas.setText("Fechas vacías: todo el historial");
        campos.add(lblAyudaFechas);
        campos.add(lblEspacio);

        cabecera.add(campos, java.awt.BorderLayout.CENTER);

        acciones.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        generar.setText("Generar reporte");
        generar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generarActionPerformed(evt);
            }
        });
        acciones.add(generar);

        limpiar.setText("Limpiar filtros");
        limpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limpiarActionPerformed(evt);
            }
        });
        acciones.add(limpiar);

        imprimir.setText("Ver PDF");
        imprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imprimirActionPerformed(evt);
            }
        });
        acciones.add(imprimir);

        cabecera.add(acciones, java.awt.BorderLayout.SOUTH);

        contenido.add(cabecera, java.awt.BorderLayout.NORTH);

        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(26);
        tabla.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        scrollResultados.setViewportView(tabla);

        contenido.add(scrollResultados, java.awt.BorderLayout.CENTER);

        pie.setLayout(new java.awt.GridLayout(2, 1));

        filtros.setText(" ");
        pie.add(filtros);

        resumen.setText("Seleccione un reporte y presione Generar reporte.");
        pie.add(resumen);

        contenido.add(pie, java.awt.BorderLayout.SOUTH);

        getContentPane().add(contenido, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tipoActionPerformed
        actualizarCampos();
    }//GEN-LAST:event_tipoActionPerformed

    private void generarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generarActionPerformed
        generar();
    }//GEN-LAST:event_generarActionPerformed

    private void limpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limpiarActionPerformed
        cuenta.setText(""); cliente.setText(""); desde.setText(""); hasta.setText(""); invalidar();
    }//GEN-LAST:event_limpiarActionPerformed

    private void imprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imprimirActionPerformed
        imprimir();
    }//GEN-LAST:event_imprimirActionPerformed

    private void actualizarCampos() {
        TipoReporte t = (TipoReporte)tipo.getSelectedItem();
        cuenta.setEnabled(t.usaCuenta()); desde.setEnabled(t.usaFechas()); hasta.setEnabled(t.usaFechas());
        invalidar();
    }
    private void invalidar() {
        reporte=null; imprimir.setEnabled(false);
        tabla.setModel(new DefaultTableModel());
        filtros.setText(" "); resumen.setText("Presione Generar reporte para consultar con los filtros actuales.");
    }
    private LocalDate fecha(JTextField campo) {
        return campo.getText().trim().isEmpty() ? null : LocalDate.parse(campo.getText().trim());
    }
    private void generar() {
        TipoReporte t=(TipoReporte)tipo.getSelectedItem();
        final LocalDate inicio,fin;
        try {
            inicio=t.usaFechas()?fecha(desde):null; fin=t.usaFechas()?fecha(hasta):null;
            if (inicio!=null && fin!=null && inicio.isAfter(fin)) throw new IllegalArgumentException("La fecha inicial debe ser anterior o igual a la final.");
            if (fin!=null && fin.equals(LocalDate.MAX)) throw new IllegalArgumentException("La fecha final está fuera del rango permitido.");
        } catch (DateTimeParseException ex) { avisar("Ingrese fechas válidas con formato AAAA-MM-DD."); return;
        } catch (IllegalArgumentException ex) { avisar(ex.getMessage()); return; }
        String numero=cuenta.getText(), persona=cliente.getText();
        ocupado(true); resumen.setText("Consultando la base de datos...");
        new SwingWorker<Reporte,Void>() {
            protected Reporte doInBackground() throws Exception { return new ReporteDAO().generar(t,numero,persona,inicio,fin); }
            protected void done() {
                ocupado(false);
                try { mostrarReporte(get()); }
                catch (InterruptedException ex) { Thread.currentThread().interrupt(); invalidar(); avisar("Consulta interrumpida."); }
                catch (ExecutionException ex) {
                    invalidar(); resumen.setText("No se pudo generar el reporte.");
                    java.util.logging.Logger.getLogger(ReportesForm.class.getName()).log(java.util.logging.Level.SEVERE,"Error al generar reporte",ex.getCause());
                    avisar("No se pudo consultar la base de datos: " + ex.getCause().getMessage());
                }
            }
        }.execute();
    }
    private void ocupado(boolean valor) {
        generar.setEnabled(!valor); tipo.setEnabled(!valor); cliente.setEnabled(!valor);
        TipoReporte t=(TipoReporte)tipo.getSelectedItem();
        cuenta.setEnabled(!valor && t.usaCuenta()); desde.setEnabled(!valor && t.usaFechas()); hasta.setEnabled(!valor && t.usaFechas());
        // Evita cambios de filtros durante una consulta en segundo plano.
        for (Component c : ((JPanel)generar.getParent()).getComponents()) c.setEnabled(!valor);
        imprimir.setEnabled(false);
    }
    /** Presenta un resultado ya consultado; tambien permite verificar el renderizado. */
    public void mostrarReporte(Reporte resultado) {
        reporte=resultado;
        titulo.setText(resultado.titulo);
        DefaultTableModel modelo=new DefaultTableModel(resultado.columnas.toArray(),0) {
            public boolean isCellEditable(int fila,int columna) { return false; }
            public Class<?> getColumnClass(int columna) {
                for (int i=0;i<getRowCount();i++) if (getValueAt(i,columna)!=null) return getValueAt(i,columna).getClass();
                return Object.class;
            }
        };
        for (java.util.List<Object> fila : resultado.filas) modelo.addRow(fila.toArray());
        tabla.setModel(modelo);
        for (int i=0;i<tabla.getColumnCount();i++) {
            String nombre=tabla.getColumnName(i);
            tabla.getColumnModel().getColumn(i).setPreferredWidth(nombre.equals("Descripcion") || nombre.equals("Direccion") ? 300 : nombre.equals("Cliente") || nombre.equals("Correo") ? 220 : nombre.equals("Fecha") ? 180 : 130);
        }
        filtros.setText(resultado.filtros);
        resumen.setText(resultado.filas.isEmpty()?"Sin resultados para los filtros seleccionados.":resultado.filas.size()+" fila(s). Importes en USD. Generado: "+java.time.LocalDateTime.now().withNano(0));
        imprimir.setEnabled(true);
    }
    private void imprimir() { generarYAbrir(); }

    /** Un clic genera y abre el archivo con la aplicacion asociada de Windows. */
    private void generarYAbrir() {
        if (reporte==null) return;
        final Reporte actual=reporte;
        ocupado(true);
        resumen.setText("Generando PDF...");
        new SwingWorker<java.nio.file.Path,Void>() {
            private String advertencia;
            protected java.nio.file.Path doInBackground() throws Exception {
                java.nio.file.Path archivo=ArchivoReporte.generar(actual);
                try {
                    if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                        advertencia="No hay una aplicación disponible para abrirlo.";
                    } else Desktop.getDesktop().open(archivo.toFile());
                } catch (java.io.IOException | RuntimeException ex) {
                    advertencia="No se pudo abrir automáticamente. Revisá la aplicación asociada al archivo.";
                    java.util.logging.Logger.getLogger(ReportesForm.class.getName()).log(java.util.logging.Level.WARNING,"Archivo generado, pero no se pudo abrir",ex);
                }
                return archivo;
            }
            protected void done() {
                ocupado(false);
                imprimir.setEnabled(reporte!=null);
                try {
                    java.nio.file.Path archivo=get();
                    resumen.setText((advertencia==null?"Archivo abierto: ":advertencia+" Guardado en: ")+archivo);
                    resumen.setToolTipText(archivo.toString());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt(); avisar("La generación fue interrumpida.");
                } catch (ExecutionException ex) {
                    resumen.setText("No se pudo generar el archivo.");
                    avisar("No se pudo generar el archivo: "+ex.getCause().getMessage());
                }
            }
        }.execute();
    }
    private void avisar(String texto) { JOptionPane.showMessageDialog(this,texto,"Reportes",JOptionPane.WARNING_MESSAGE); }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReportesForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel acciones;
    private javax.swing.JPanel cabecera;
    private javax.swing.JPanel campos;
    private javax.swing.JTextField cliente;
    private javax.swing.JPanel contenido;
    private javax.swing.JTextField cuenta;
    private javax.swing.JTextField desde;
    private javax.swing.JLabel filtros;
    private javax.swing.JButton generar;
    private javax.swing.JTextField hasta;
    private javax.swing.JButton imprimir;
    private javax.swing.JLabel lblAyudaFechas;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblCuenta;
    private javax.swing.JLabel lblDesde;
    private javax.swing.JLabel lblEspacio;
    private javax.swing.JLabel lblHasta;
    private javax.swing.JLabel lblReporte;
    private javax.swing.JButton limpiar;
    private javax.swing.JPanel pie;
    private javax.swing.JLabel resumen;
    private javax.swing.JScrollPane scrollResultados;
    private javax.swing.JTable tabla;
    private javax.swing.JComboBox<com.banco.reporte.TipoReporte> tipo;
    private javax.swing.JLabel titulo;
    // End of variables declaration//GEN-END:variables
}

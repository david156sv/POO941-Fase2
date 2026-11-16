package com.banco.util.vista;

import com.banco.excepcion.OperacionBancariaException;
import com.banco.servicio.TransaccionService;
import com.banco.servicio.TransferenciaService;
import com.banco.util.dao.ConsultaMovimientoDAO;
import com.banco.util.modelo.Cuenta;
import com.banco.util.modelo.Movimiento;
import com.banco.util.modelo.TipoMovimiento;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Persona 2: formulario de transferencias entre cuentas y consulta de movimientos con filtros.
 * Pestana 1 "Transferir"  -> usa TransferenciaService.
 * Pestana 2 "Movimientos" -> usa ConsultaMovimientoDAO (doble clic en una fila = detalle).
 */
public class TransferenciasForm extends JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(TransferenciasForm.class.getName());
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String TODOS = "(Todos)";

    private final TransferenciaService transferencias = new TransferenciaService();
    private final TransaccionService cuentas = new TransaccionService();   // solo para consultar titular/saldo
    private final ConsultaMovimientoDAO consultaDao = new ConsultaMovimientoDAO();

    // --- Pestana Transferir ---
    private final JTextField txtOrigen = new JTextField(14);
    private final JTextField txtDestino = new JTextField(14);
    private final JTextField txtMonto = new JTextField(10);
    private final JTextField txtMotivo = new JTextField(30);
    private final JLabel lblOrigen = new JLabel("—");
    private final JLabel lblDestino = new JLabel("—");
    private final JLabel lblMensaje = new JLabel(" ");

    // --- Pestana Movimientos ---
    private final JTextField txtFiltroCuenta = new JTextField(12);
    private final JComboBox<String> cmbFiltroTipo = new JComboBox<>();
    private final JTextField txtDesde = new JTextField(8);
    private final JTextField txtHasta = new JTextField(8);
    private final JLabel lblResumen = new JLabel(" ");
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Cuenta", "Fecha", "Tipo", "Monto", "Saldo resultante", "Descripción"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public TransferenciasForm() {
        setTitle("Banco Académico UDB - Transferencias y Movimientos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        java.net.URL logo = getClass().getResource("/com/banco/img/logo.jfif");
        if (logo != null) {
            setIconImage(new javax.swing.ImageIcon(logo).getImage());
        }

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Transferir", construirPanelTransferir());
        pestanas.addTab("Movimientos", construirPanelMovimientos());
        add(pestanas);

        setSize(960, 520);
        setLocationRelativeTo(null);
    }

    // ===================================================================== TRANSFERIR

    private JPanel construirPanelTransferir() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        JButton btnVerOrigen = new JButton("Verificar");
        JButton btnVerDestino = new JButton("Verificar");
        JButton btnTransferir = new JButton("Transferir");
        JButton btnLimpiar = new JButton("Limpiar");

        fila(p, g, 0, "Cuenta origen:", txtOrigen, btnVerOrigen);
        g.gridx = 1; g.gridy = 1; g.gridwidth = 3; p.add(lblOrigen, g);
        fila(p, g, 2, "Cuenta destino:", txtDestino, btnVerDestino);
        g.gridx = 1; g.gridy = 3; g.gridwidth = 3; p.add(lblDestino, g);
        fila(p, g, 4, "Monto:", txtMonto, null);
        g.gridx = 0; g.gridy = 5; g.gridwidth = 1; p.add(new JLabel("Motivo (opcional):"), g);
        g.gridx = 1; g.gridwidth = 3; p.add(txtMotivo, g);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        botones.add(btnTransferir);
        botones.add(btnLimpiar);
        g.gridx = 1; g.gridy = 6; g.gridwidth = 3; p.add(botones, g);
        g.gridx = 0; g.gridy = 7; g.gridwidth = 4; p.add(lblMensaje, g);

        // empuja todo hacia arriba
        g.gridy = 8; g.weighty = 1; p.add(new JLabel(), g);

        btnVerOrigen.addActionListener(e -> verificar(txtOrigen, lblOrigen, true));
        btnVerDestino.addActionListener(e -> verificar(txtDestino, lblDestino, false));
        btnTransferir.addActionListener(e -> ejecutarTransferencia());
        btnLimpiar.addActionListener(e -> limpiarTransferencia());
        return p;
    }

    private void fila(JPanel p, GridBagConstraints g, int y, String texto, JTextField campo, JButton boton) {
        g.gridy = y; g.gridwidth = 1;
        g.gridx = 0; p.add(new JLabel(texto), g);
        g.gridx = 1; p.add(campo, g);
        if (boton != null) {
            g.gridx = 2; p.add(boton, g);
        }
    }

    /** Muestra titular y estado de la cuenta (y el saldo solo para la cuenta origen). */
    private void verificar(JTextField campo, JLabel destino, boolean mostrarSaldo) {
        String numero = campo.getText().trim();
        if (numero.isEmpty()) {
            avisar("Escriba el número de cuenta.");
            return;
        }
        try {
            Cuenta c = cuentas.consultarPorNumero(numero);
            if (c == null) {
                destino.setText("No existe ninguna cuenta con el número " + numero);
                return;
            }
            destino.setText(c.getCodigoCliente() + " - " + c.getNombreCliente()
                    + "  |  " + c.getEstado().getEtiqueta()
                    + (mostrarSaldo ? "  |  Saldo: " + c.getSaldo().toPlainString() : ""));
        } catch (SQLException ex) {
            mostrarErrorBD("consultar la cuenta", ex);
        }
    }

    private void ejecutarTransferencia() {
        BigDecimal monto;
        try {
            monto = new BigDecimal(txtMonto.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            avisar("El monto debe ser un número decimal, por ejemplo: 250.00");
            return;
        }
        String origen = txtOrigen.getText().trim();
        String destino = txtDestino.getText().trim();

        int ok = JOptionPane.showConfirmDialog(this,
                "¿Transferir " + monto.toPlainString() + " de la cuenta " + origen
                + " a la cuenta " + destino + "?",
                "Confirmar transferencia", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Movimiento mov = transferencias.transferir(origen, destino, monto, txtMotivo.getText());
            lblMensaje.setText("Transferencia realizada. Nuevo saldo de la cuenta origen: "
                    + mov.getSaldoResultante().toPlainString());
            JOptionPane.showMessageDialog(this, "Transferencia realizada con éxito.\nNuevo saldo de la cuenta "
                    + origen + ": " + mov.getSaldoResultante().toPlainString(),
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            txtMonto.setText("");
            txtMotivo.setText("");
            verificar(txtOrigen, lblOrigen, true);       // refresca el saldo mostrado
        } catch (OperacionBancariaException ex) {
            lblMensaje.setText("Operación rechazada: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Operación rechazada", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error de BD en transferencia", ex);
            mostrarErrorBD("realizar la transferencia", ex);
        }
    }

    private void limpiarTransferencia() {
        txtOrigen.setText("");
        txtDestino.setText("");
        txtMonto.setText("");
        txtMotivo.setText("");
        lblOrigen.setText("—");
        lblDestino.setText("—");
        lblMensaje.setText(" ");
        txtOrigen.requestFocus();
    }

    // ===================================================================== MOVIMIENTOS

    private JPanel construirPanelMovimientos() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        cmbFiltroTipo.setModel(new DefaultComboBoxModel<>(new String[]{
            TODOS, TipoMovimiento.DEPOSITO.getEtiqueta(),
            TipoMovimiento.RETIRO.getEtiqueta(), TipoMovimiento.TRANSFERENCIA.getEtiqueta()}));

        JButton btnBuscar = new JButton("Buscar");
        JButton btnLimpiar = new JButton("Limpiar");

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filtros.add(new JLabel("Cuenta:"));
        filtros.add(txtFiltroCuenta);
        filtros.add(new JLabel("Tipo:"));
        filtros.add(cmbFiltroTipo);
        filtros.add(new JLabel("Desde (aaaa-mm-dd):"));
        filtros.add(txtDesde);
        filtros.add(new JLabel("Hasta (aaaa-mm-dd):"));
        filtros.add(txtHasta);
        filtros.add(btnBuscar);
        filtros.add(btnLimpiar);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setReorderingAllowed(false);
        int[] anchos = {50, 110, 120, 100, 90, 110, 380};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.getSelectedRow() >= 0) {
                    mostrarDetalle((Integer) modeloTabla.getValueAt(tabla.getSelectedRow(), 0));
                }
            }
        });

        p.add(filtros, BorderLayout.NORTH);
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);
        p.add(lblResumen, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> buscarMovimientos());
        btnLimpiar.addActionListener(e -> limpiarMovimientos());
        return p;
    }

    private void buscarMovimientos() {
        LocalDate desde;
        LocalDate hasta;
        try {
            desde = parsearFecha(txtDesde.getText());
            hasta = parsearFecha(txtHasta.getText());
        } catch (DateTimeParseException ex) {
            avisar("Use el formato de fecha aaaa-mm-dd, por ejemplo: 2026-11-11");
            return;
        }
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            avisar("La fecha \"Desde\" no puede ser posterior a \"Hasta\".");
            return;
        }
        String seleccionado = (String) cmbFiltroTipo.getSelectedItem();
        TipoMovimiento tipo = TODOS.equals(seleccionado) ? null : TipoMovimiento.desdeEtiqueta(seleccionado);

        try {
            List<Movimiento> lista = consultaDao.buscar(txtFiltroCuenta.getText(), tipo, desde, hasta);
            modeloTabla.setRowCount(0);
            for (Movimiento m : lista) {
                modeloTabla.addRow(new Object[]{
                    m.getIdMovimiento(),
                    m.getNumeroCuenta(),
                    m.getFechaHora() == null ? "" : m.getFechaHora().format(FORMATO_FECHA_HORA),
                    m.getTipoMovimiento(),
                    m.getMonto().toPlainString(),
                    m.getSaldoResultante().toPlainString(),
                    m.getDescripcion()
                });
            }
            lblResumen.setText(lista.size() + " movimiento(s) encontrado(s). Doble clic en una fila para ver el detalle.");
        } catch (SQLException ex) {
            mostrarErrorBD("consultar los movimientos", ex);
        }
    }

    private void mostrarDetalle(int idMovimiento) {
        try {
            Movimiento m = consultaDao.buscarPorId(idMovimiento);
            if (m == null) {
                avisar("El movimiento ya no existe.");
                return;
            }
            String texto = "Movimiento N.º " + m.getIdMovimiento()
                    + "\nCuenta: " + m.getNumeroCuenta()
                    + "\nTipo: " + m.getTipoMovimiento()
                    + "\nMonto: " + m.getMonto().toPlainString()
                    + "\nSaldo resultante: " + m.getSaldoResultante().toPlainString()
                    + "\nFecha y hora: " + (m.getFechaHora() == null ? "" : m.getFechaHora().format(FORMATO_FECHA_HORA))
                    + "\nDescripción: " + m.getDescripcion();
            JOptionPane.showMessageDialog(this, texto, "Detalle del movimiento", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            mostrarErrorBD("consultar el movimiento", ex);
        }
    }

    private void limpiarMovimientos() {
        txtFiltroCuenta.setText("");
        txtDesde.setText("");
        txtHasta.setText("");
        cmbFiltroTipo.setSelectedIndex(0);
        modeloTabla.setRowCount(0);
        lblResumen.setText(" ");
    }

    private LocalDate parsearFecha(String texto) {
        return (texto == null || texto.trim().isEmpty()) ? null : LocalDate.parse(texto.trim());
    }

    // ===================================================================== utilidades

    private void avisar(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Aviso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarErrorBD(String accion, SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "No se pudo " + accion + ".\nVerifique que MySQL esté encendido y que ConexionDB sea correcto.\n"
              + "Detalle: " + ex.getMessage(),
                "Error de base de datos", JOptionPane.ERROR_MESSAGE);
    }

    /** Para probar este formulario solo, sin pasar por el resto del sistema. */
    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> {
            TransferenciasForm f = new TransferenciasForm();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);
        });
    }
}

package com.banco.util.vista;

import java.awt.Window;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/** Acceso compartido a reportes y transferencias desde todas las pantallas. */
public final class MenuReportes {
    private MenuReportes() { }

    public static void instalar(JFrame ventana) {
        JMenuBar barra = ventana.getJMenuBar();
        if (barra == null) barra = new JMenuBar();

        if (!(ventana instanceof TransferenciasForm)) {
            JMenu menuTransf = new JMenu("Transferencias");
            JMenuItem abrirTransf = new JMenuItem("Transferencias y movimientos");
            abrirTransf.addActionListener(e -> abrirTransferencias(ventana));
            menuTransf.add(abrirTransf);
            barra.add(menuTransf);
        }

        JMenu menu = new JMenu("Reportes");
        JMenuItem abrir = new JMenuItem("Reportes financieros y de cuentas");
        abrir.addActionListener(e -> abrirReportes(ventana));
        menu.add(abrir);
        barra.add(menu);
        ventana.setJMenuBar(barra);
    }

    private static void abrirTransferencias(JFrame origen) {
        for (Window ventana : Window.getWindows()) {
            if (ventana instanceof TransferenciasForm && ventana.isDisplayable()) {
                ventana.setVisible(true);
                ((JFrame) ventana).setState(JFrame.NORMAL);
                ventana.toFront();
                ventana.requestFocus();
                return;
            }
        }
        TransferenciasForm transferencias = new TransferenciasForm();
        transferencias.setLocationRelativeTo(origen);
        transferencias.setVisible(true);
    }

    private static void abrirReportes(JFrame origen) {
        for (Window ventana : Window.getWindows()) {
            if (ventana instanceof ReportesForm && ventana.isDisplayable()) {
                ventana.setVisible(true);
                ((JFrame) ventana).setState(JFrame.NORMAL);
                ventana.toFront();
                ventana.requestFocus();
                return;
            }
        }
        ReportesForm reportes = new ReportesForm();
        reportes.setLocationRelativeTo(origen);
        reportes.setVisible(true);
    }
}

package com.banco.util.vista;

import java.awt.Window;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/** Acceso compartido a los reportes desde todas las pantallas. */
public final class MenuReportes {
    private MenuReportes() { }

    public static void instalar(JFrame ventana) {
        JMenuBar barra = ventana.getJMenuBar();
        if (barra == null) barra = new JMenuBar();

        JMenu menu = new JMenu("Reportes");
        JMenuItem abrir = new JMenuItem("Reportes financieros y de cuentas");
        abrir.addActionListener(e -> abrirReportes(ventana));
        menu.add(abrir);
        barra.add(menu);
        ventana.setJMenuBar(barra);
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

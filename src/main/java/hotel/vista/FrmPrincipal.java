package hotel.vista;

import hotel.servicio.CheckOutService;
import hotel.servicio.ClienteService;
import hotel.servicio.CotizadorService;
import hotel.servicio.EstadiaService;
import hotel.servicio.HabitacionService;
import hotel.servicio.ReservaService;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class FrmPrincipal extends JFrame {

    public FrmPrincipal(ClienteService clientes, HabitacionService habitaciones,
            ReservaService reservas, EstadiaService estadias,
            CheckOutService checkOut, CotizadorService cotizador) {
        setTitle("Sistema de Gestión de Hospedaje");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(360, 390);
        setLocationRelativeTo(null);

        JButton btnClientes = new JButton("Clientes");
        JButton btnHabitaciones = new JButton("Habitaciones");
        JButton btnReservas = new JButton("Reservas");
        JButton btnEstadias = new JButton("Estadías (check-in, consumos, check-out)");
        JButton btnCotizador = new JButton("Cotizar tarifas");

        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        panel.add(btnClientes);
        panel.add(btnHabitaciones);
        panel.add(btnReservas);
        panel.add(btnEstadias);
        panel.add(btnCotizador);
        add(panel);

        btnClientes.addActionListener(e -> new FrmClientes(clientes).setVisible(true));
        btnHabitaciones.addActionListener(e -> new FrmHabitaciones(habitaciones).setVisible(true));
        btnReservas.addActionListener(e -> new FrmReservas(reservas).setVisible(true));
        btnEstadias.addActionListener(e -> new FrmEstadias(estadias, checkOut).setVisible(true));
        btnCotizador.addActionListener(e -> new FrmCotizador(cotizador).setVisible(true));
    }
}

package hotel.vista;

import hotel.servicio.ClienteService;
import hotel.servicio.HabitacionService;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class FrmPrincipal extends JFrame {

    public FrmPrincipal(ClienteService clientes, HabitacionService habitaciones) {
        setTitle("Sistema de Gestión de Hospedaje");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(360, 220);
        setLocationRelativeTo(null);

        JButton btnClientes = new JButton("Clientes");
        JButton btnHabitaciones = new JButton("Habitaciones");

        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        panel.add(btnClientes);
        panel.add(btnHabitaciones);
        add(panel);

        btnClientes.addActionListener(e -> new FrmClientes(clientes).setVisible(true));
        btnHabitaciones.addActionListener(e -> new FrmHabitaciones(habitaciones).setVisible(true));
    }
}

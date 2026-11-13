package hotel.vista;

import hotel.seguridad.Modulo;
import hotel.seguridad.PoliticaAcceso;
import hotel.seguridad.Usuario;
import hotel.servicio.Servicios;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FrmPrincipal extends JFrame {

    private final Usuario usuario;
    private final PoliticaAcceso politica;

    public FrmPrincipal(Usuario usuario, PoliticaAcceso politica, Servicios servicios,
            Runnable alCerrarSesion) {
        this.usuario = usuario;
        this.politica = politica;

        setTitle("Sistema de Gestión de Hospedaje");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel lblUsuario = new JLabel("Usuario: " + usuario.nombre() + "  |  Rol: " + usuario.rol());
        lblUsuario.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));
        add(lblUsuario, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        panel.add(boton("Clientes", Modulo.CLIENTES,
                () -> new FrmClientes(servicios.clientes()).setVisible(true)));
        panel.add(boton("Habitaciones", Modulo.HABITACIONES,
                () -> new FrmHabitaciones(servicios.habitaciones()).setVisible(true)));
        panel.add(boton("Reservas", Modulo.RESERVAS,
                () -> new FrmReservas(servicios.reservas(), usuario.id()).setVisible(true)));
        panel.add(boton("Estadías (check-in, consumos, check-out)", Modulo.ESTADIAS,
                () -> new FrmEstadias(servicios.estadias(), servicios.checkOut(), usuario.id())
                        .setVisible(true)));
        panel.add(boton("Operaciones (limpieza y mantenimiento)", Modulo.OPERACIONES,
                () -> new FrmOperaciones(servicios.limpieza(), servicios.mantenimiento(),
                        servicios.habitaciones(), usuario.id()).setVisible(true)));
        panel.add(boton("Cotizar tarifas", Modulo.COTIZADOR,
                () -> new FrmCotizador(servicios.cotizador()).setVisible(true)));
        add(panel, BorderLayout.CENTER);

        JButton btnCerrarSesion = new JButton("Cerrar sesión");
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sur.add(btnCerrarSesion);
        add(sur, BorderLayout.SOUTH);

        btnCerrarSesion.addActionListener(e -> {
            Window[] anteriores = Window.getWindows();
            alCerrarSesion.run();
            for (Window ventana : anteriores) {
                ventana.dispose();
            }
        });
    }

    private JButton boton(String texto, Modulo modulo, Runnable accion) {
        JButton boton = new JButton(texto);
        boolean permitido = politica.permite(usuario.rol(), modulo);
        boton.setEnabled(permitido);
        if (permitido) {
            boton.addActionListener(e -> accion.run());
        } else {
            boton.setToolTipText("El rol " + usuario.rol() + " no tiene acceso a este módulo.");
        }
        return boton;
    }
}

package hotel.vista;

import hotel.modelo.seguridad.Modulo;
import hotel.modelo.seguridad.PoliticaAcceso;
import hotel.modelo.seguridad.Usuario;
import hotel.controlador.Servicios;
import hotel.vista.estilo.Encabezado;
import hotel.vista.estilo.Iconos;
import hotel.vista.estilo.Tema;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
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
        setSize(1020, 640);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(new Encabezado("Bienvenido, " + usuario.nombre(), fechaHoy(), Iconos.HOTEL,
                chipRol(usuario.rol())), BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(2, 4, 18, 18));
        panel.setOpaque(false);
        panel.add(modulo("Clientes", "Registro de huéspedes", Iconos.CLIENTES, Modulo.CLIENTES,
                () -> new FrmClientes(servicios.clientes()).setVisible(true)));
        panel.add(modulo("Habitaciones", "Inventario y estados", Iconos.HABITACIONES,
                Modulo.HABITACIONES, () -> new FrmHabitaciones(servicios.habitaciones()).setVisible(true)));
        panel.add(modulo("Reservas", "Gestión de reservas", Iconos.RESERVAS, Modulo.RESERVAS,
                () -> new FrmReservas(servicios.reservas(), usuario.id()).setVisible(true)));
        panel.add(modulo("Estadías", "Check-in, consumos y check-out", Iconos.ESTADIAS, Modulo.ESTADIAS,
                () -> new FrmEstadias(servicios.estadias(), servicios.checkOut(), usuario.id())
                        .setVisible(true)));
        panel.add(modulo("Operaciones", "Limpieza y mantenimiento", Iconos.OPERACIONES, Modulo.OPERACIONES,
                () -> new FrmOperaciones(servicios.limpieza(), servicios.mantenimiento(),
                        servicios.habitaciones(), usuario.id()).setVisible(true)));
        panel.add(modulo("Cotizar tarifas", "Precio de una estadía", Iconos.COTIZADOR,
                Modulo.COTIZADOR, () -> new FrmCotizador(servicios.cotizador()).setVisible(true)));
        panel.add(modulo("Reportes", "Indicadores gerenciales", Iconos.REPORTES,
                Modulo.REPORTES, () -> new FrmReportes(servicios.reportes(), usuario.nombre()).setVisible(true)));
        panel.add(new TarjetaModulo("Cerrar sesión", "Volver al ingreso", Iconos.SALIR, true,
                true, null, () -> {
                    Window[] anteriores = Window.getWindows();
                    alCerrarSesion.run();
                    for (Window ventana : anteriores) {
                        ventana.dispose();
                    }
                }));

        JLabel titulo = new JLabel("Módulos del sistema");
        titulo.setFont(Tema.fuente(Font.BOLD, 16));
        titulo.setForeground(Tema.NAVY);
        JPanel cuerpo = Tema.cuerpo();
        cuerpo.add(titulo, BorderLayout.NORTH);
        cuerpo.add(panel, BorderLayout.CENTER);
        add(cuerpo, BorderLayout.CENTER);

        Tema.preparar(this, Iconos.HOTEL);
    }

    private static String fechaHoy() {
        String s = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-PE")));
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private JComponent chipRol(String rol) {
        JLabel chip = new JLabel("  " + rol + "  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(200, 162, 74, 45));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1,
                        getHeight(), getHeight()));
                g2.setColor(Tema.DORADO);
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 2, getHeight() - 2,
                        getHeight(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setFont(Tema.fuente(Font.BOLD, 12));
        chip.setForeground(new Color(0xF1DFA8));
        chip.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return chip;
    }

    private TarjetaModulo modulo(String texto, String descripcion, Iconos icono, Modulo modulo,
            Runnable accion) {
        boolean permitido = politica.permite(usuario.rol(), modulo);
        return new TarjetaModulo(texto, descripcion, icono, permitido, false,
                permitido ? null : "El rol " + usuario.rol() + " no tiene acceso a este módulo.", accion);
    }

    /** Tarjeta clicable de un módulo: icono, título, descripción y efecto al pasar el mouse. */
    private static final class TarjetaModulo extends JPanel {

        private final String titulo;
        private final String descripcion;
        private final Iconos icono;
        private final boolean habilitada;
        private final boolean peligro;
        private boolean encima;

        TarjetaModulo(String titulo, String descripcion, Iconos icono, boolean habilitada,
                boolean peligro, String ayuda, Runnable accion) {
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.icono = icono;
            this.habilitada = habilitada;
            this.peligro = peligro;
            setOpaque(false);
            setToolTipText(ayuda);
            if (habilitada) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        encima = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        encima = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        accion.run();
                    }
                });
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int subida = encima ? 3 : 0;

            Color acento = !habilitada ? new Color(0xB9B6AD) : peligro ? Tema.ROJO : Tema.DORADO;
            g2.setColor(new Color(0, 0, 0, encima ? 22 : 10));
            g2.fill(new RoundRectangle2D.Double(2, 6 - subida, w - 4, h - 8, 20, 20));
            g2.setColor(habilitada ? Color.WHITE : new Color(0xEFECE4));
            g2.fill(new RoundRectangle2D.Double(0, 2 - subida, w, h - 6, 18, 18));
            g2.setColor(encima ? acento : Tema.BORDE);
            g2.draw(new RoundRectangle2D.Double(0.5, 2.5 - subida, w - 1, h - 7, 18, 18));

            int y0 = 2 - subida;
            g2.setColor(acento);
            g2.fill(new RoundRectangle2D.Double(24, y0 + 1, 36, 4, 4, 4));

            int d = 64;
            int ix = 24;
            int bloque = 64 + 56;
            int iy = y0 + Math.max(24, (h - 6 - bloque) / 2);
            g2.setColor(habilitada ? (peligro ? new Color(0xF6E3E5) : Tema.NAVY) : new Color(0xD9D5CA));
            g2.fill(new Ellipse2D.Double(ix, iy, d, d));
            icono.pintar(g2, ix + 16, iy + 16, 32,
                    habilitada ? (peligro ? Tema.ROJO : Tema.DORADO) : new Color(0xFFFFFF));

            g2.setColor(habilitada ? Tema.NAVY : new Color(0x8D8A82));
            g2.setFont(Tema.fuente(Font.BOLD, 16));
            g2.drawString(titulo, 24, iy + d + 28);
            g2.setColor(habilitada ? Tema.TEXTO_SUAVE : new Color(0xA5A299));
            g2.setFont(Tema.fuente(Font.PLAIN, 12));
            g2.drawString(habilitada ? descripcion : "Sin acceso para tu rol", 24, iy + d + 48);
            g2.dispose();
        }
    }
}

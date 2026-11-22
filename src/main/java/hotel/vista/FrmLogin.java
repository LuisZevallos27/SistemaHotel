package hotel.vista;

import hotel.excepcion.AccesoDatosException;
import hotel.excepcion.NegocioException;
import hotel.modelo.seguridad.Usuario;
import hotel.controlador.AutenticacionService;
import hotel.vista.estilo.Iconos;
import hotel.vista.estilo.Tema;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class FrmLogin extends JFrame {

    private final AutenticacionService servicio;
    private final Consumer<Usuario> alIngresar;
    private final JTextField txtUsuario = new JTextField(15);
    private final JPasswordField txtClave = new JPasswordField(15);

    public FrmLogin(AutenticacionService servicio, Consumer<Usuario> alIngresar) {
        this.servicio = servicio;
        this.alIngresar = alIngresar;

        setTitle("Ingreso al sistema - Hotel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 480);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        add(new PanelMarca(), BorderLayout.WEST);
        add(crearFormulario(), BorderLayout.CENTER);

        Tema.preparar(this, Iconos.HOTEL);
        getContentPane().setBackground(Color.WHITE);
    }

    private JPanel crearFormulario() {
        JButton btnIngresar = Tema.boton("Ingresar", Tema.Variante.PRIMARIO);
        btnIngresar.setFont(Tema.fuente(Font.BOLD, 14));

        JLabel titulo = new JLabel("Bienvenido");
        titulo.setFont(Tema.fuente(Font.BOLD, 28));
        titulo.setForeground(Tema.NAVY);
        JLabel subtitulo = new JLabel("Ingrese sus credenciales para continuar");
        subtitulo.setFont(Tema.fuente(Font.PLAIN, 13));
        subtitulo.setForeground(Tema.TEXTO_SUAVE);

        JPanel barra = new JPanel();
        barra.setBackground(Tema.DORADO);
        barra.setPreferredSize(new Dimension(40, 3));
        barra.setMaximumSize(new Dimension(40, 3));

        JPanel cabecera = new JPanel(new BorderLayout(0, 6));
        cabecera.setOpaque(false);
        JPanel textos = new JPanel(new java.awt.GridLayout(2, 1, 0, 2));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(subtitulo);
        JPanel marcoBarra = new JPanel(new BorderLayout());
        marcoBarra.setOpaque(false);
        marcoBarra.add(barra, BorderLayout.WEST);
        cabecera.add(textos, BorderLayout.CENTER);
        cabecera.add(marcoBarra, BorderLayout.SOUTH);

        JLabel pie = new JLabel("Sistema de Gestión de Hospedaje · Diseño de Patrones");
        pie.setFont(Tema.fuente(Font.PLAIN, 11));
        pie.setForeground(Tema.TEXTO_SUAVE);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 26, 0);
        p.add(cabecera, c);
        c.insets = new Insets(0, 0, 16, 0);
        p.add(Tema.campo("USUARIO", txtUsuario), c);
        p.add(Tema.campo("CONTRASEÑA", txtClave), c);
        c.insets = new Insets(8, 0, 22, 0);
        p.add(btnIngresar, c);
        c.insets = new Insets(0, 0, 0, 0);
        p.add(pie, c);

        getRootPane().setDefaultButton(btnIngresar);
        btnIngresar.addActionListener(e -> ingresar());
        return p;
    }

    private void ingresar() {
        char[] clave = txtClave.getPassword();
        try {
            Usuario usuario = servicio.iniciarSesion(txtUsuario.getText(), clave);
            dispose();
            alIngresar.accept(usuario);
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "No se pudo ingresar", JOptionPane.WARNING_MESSAGE);
            txtClave.setText("");
            txtClave.requestFocus();
        } catch (AccesoDatosException ex) {
            String detalle = ex.getCause() != null ? "\n" + ex.getCause().getMessage() : "";
            JOptionPane.showMessageDialog(this, ex.getMessage() + detalle,
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(clave, ' ');
        }
    }

    /** Panel lateral con la marca del hotel, degradado y adornos. */
    private static final class PanelMarca extends JPanel {

        PanelMarca() {
            setPreferredSize(new Dimension(340, 480));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, Tema.NAVY, w, h, Tema.NAVY_CLARO));
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(255, 255, 255, 12));
            g2.fill(new Ellipse2D.Double(-120, h - 260, 380, 380));
            g2.setColor(new Color(200, 162, 74, 40));
            g2.fill(new Ellipse2D.Double(w - 150, -90, 260, 260));
            g2.setColor(new Color(255, 255, 255, 10));
            g2.fill(new Ellipse2D.Double(w - 120, h - 150, 220, 220));

            g2.setColor(Tema.DORADO);
            g2.fillRect(w - 4, 0, 4, h);

            int cx = w / 2;
            g2.setColor(new Color(200, 162, 74, 55));
            g2.fill(new Ellipse2D.Double(cx - 48, 96, 96, 96));
            g2.setColor(Tema.DORADO);
            g2.fill(new Ellipse2D.Double(cx - 40, 104, 80, 80));
            Iconos.HOTEL.pintar(g2, cx - 22, 122, 44, Tema.NAVY);

            g2.setColor(Color.WHITE);
            g2.setFont(Tema.fuente(Font.BOLD, 34));
            centrar(g2, "HOTEL", cx, 238);

            g2.setColor(Tema.DORADO);
            for (int i = 0; i < 5; i++) {
                Iconos.estrella(g2, cx - 44 + i * 22, 262, 7);
            }

            g2.setColor(new Color(0xC3CEDD));
            g2.setFont(Tema.fuente(Font.PLAIN, 14));
            centrar(g2, "Sistema de Gestión de Hospedaje", cx, 304);
            g2.setFont(Tema.fuente(Font.PLAIN, 12));
            g2.setColor(new Color(0x8FA2BB));
            centrar(g2, "Reservas · Estadías · Facturación", cx, 328);

            g2.setColor(new Color(255, 255, 255, 40));
            g2.draw(new RoundRectangle2D.Double(cx - 60, 358, 120, 1, 1, 1));
            g2.dispose();
        }

        private void centrar(Graphics2D g2, String texto, int cx, int y) {
            g2.drawString(texto, cx - g2.getFontMetrics().stringWidth(texto) / 2, y);
        }
    }
}

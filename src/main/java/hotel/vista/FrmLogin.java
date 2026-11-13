package hotel.vista;

import hotel.excepcion.AccesoDatosException;
import hotel.excepcion.NegocioException;
import hotel.modelo.seguridad.Usuario;
import hotel.controlador.AutenticacionService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
        setSize(360, 220);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(25, 30, 10, 30));
        form.add(new JLabel("Usuario:"));
        form.add(txtUsuario);
        form.add(new JLabel("Contraseña:"));
        form.add(txtClave);

        JButton btnIngresar = new JButton("Ingresar");
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sur.add(btnIngresar);

        add(form, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnIngresar);
        btnIngresar.addActionListener(e -> ingresar());
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
}

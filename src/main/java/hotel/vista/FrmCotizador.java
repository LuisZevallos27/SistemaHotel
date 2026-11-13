package hotel.vista;

import hotel.excepcion.AccesoDatosException;
import hotel.excepcion.NegocioException;
import hotel.controlador.CotizadorService;
import hotel.modelo.tarifa.Cotizacion;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class FrmCotizador extends JFrame {

    private static final String SIN_CANAL = "(sin canal)";

    private final CotizadorService servicio;

    private final JComboBox<String> cboTipo;
    private final JComboBox<String> cboCanal;
    private final JTextField txtEntrada = new JTextField(LocalDate.now().plusDays(1).toString());
    private final JTextField txtSalida = new JTextField(LocalDate.now().plusDays(3).toString());
    private final JTextField txtDocumento = new JTextField();
    private final JTextField txtPromocion = new JTextField();
    private final JTextArea txtResultado = new JTextArea(14, 50);

    public FrmCotizador(CotizadorService servicio) {
        this.servicio = servicio;
        this.cboTipo = new JComboBox<>(servicio.tiposHabitacion().toArray(new String[0]));
        this.cboCanal = new JComboBox<>();
        cboCanal.addItem(SIN_CANAL);
        for (String canal : servicio.canales()) {
            cboCanal.addItem(canal);
        }

        setTitle("Cotizador de estadía - Hotel");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(640, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));
        form.setBorder(BorderFactory.createTitledBorder("Datos de la cotización"));
        form.add(new JLabel("Tipo de habitación:"));
        form.add(cboTipo);
        form.add(new JLabel("Canal de reserva:"));
        form.add(cboCanal);
        form.add(new JLabel("Entrada (AAAA-MM-DD):"));
        form.add(txtEntrada);
        form.add(new JLabel("Salida (AAAA-MM-DD):"));
        form.add(txtSalida);
        form.add(new JLabel("Documento del cliente (opcional):"));
        form.add(txtDocumento);
        form.add(new JLabel("Código de promoción (opcional):"));
        form.add(txtPromocion);

        JButton btnCotizar = new JButton("Cotizar");
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(form, BorderLayout.CENTER);
        norte.add(btnCotizar, BorderLayout.SOUTH);

        txtResultado.setEditable(false);
        txtResultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(txtResultado), BorderLayout.CENTER);

        btnCotizar.addActionListener(e -> cotizar());
    }

    private void cotizar() {
        try {
            LocalDate entrada = LocalDate.parse(txtEntrada.getText().trim());
            LocalDate salida = LocalDate.parse(txtSalida.getText().trim());
            String canal = SIN_CANAL.equals(cboCanal.getSelectedItem())
                    ? "" : (String) cboCanal.getSelectedItem();
            Cotizacion c = servicio.cotizar((String) cboTipo.getSelectedItem(), entrada, salida,
                    txtDocumento.getText(), txtPromocion.getText(), canal);
            mostrar(c);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Use el formato de fecha AAAA-MM-DD.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (AccesoDatosException ex) {
            String detalle = ex.getCause() != null ? "\n" + ex.getCause().getMessage() : "";
            JOptionPane.showMessageDialog(this, ex.getMessage() + detalle,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrar(Cotizacion c) {
        StringBuilder sb = new StringBuilder();
        sb.append("Habitación ").append(c.tipo()).append(" | ")
                .append(c.noches()).append(" noche(s)\n\n");
        sb.append("Cálculo de la primera noche:\n");
        for (String linea : c.calculoPrimeraNoche()) {
            sb.append("  - ").append(linea).append('\n');
        }
        sb.append("\nPrecio por noche:\n");
        for (String linea : c.precioPorNoche()) {
            sb.append("  ").append(linea).append('\n');
        }
        sb.append(String.format(Locale.US, "%nTOTAL: S/ %.2f", c.total()));
        txtResultado.setText(sb.toString());
        txtResultado.setCaretPosition(0);
    }
}

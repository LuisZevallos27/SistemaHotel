package hotel.vista;

import hotel.excepcion.AccesoDatosException;
import hotel.excepcion.NegocioException;
import hotel.controlador.CotizadorService;
import hotel.modelo.tarifa.Cotizacion;
import hotel.vista.estilo.Encabezado;
import hotel.vista.estilo.Iconos;
import hotel.vista.estilo.Tarjeta;
import hotel.vista.estilo.Tema;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
    private final JLabel lblTotal = new JLabel("S/ 0.00");

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
        setSize(1020, 680);
        setMinimumSize(new java.awt.Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JButton btnCotizar = Tema.boton("Cotizar", Tema.Variante.ACENTO, Iconos.COTIZADOR);

        JPanel form = Tema.apilar(12,
                Tema.campo("Tipo de habitación", cboTipo),
                Tema.campo("Canal de reserva", cboCanal),
                Tema.fila(new int[]{1, 1},
                        Tema.campo("Entrada (AAAA-MM-DD)", txtEntrada),
                        Tema.campo("Salida (AAAA-MM-DD)", txtSalida)),
                Tema.campo("Documento del cliente (opcional)", txtDocumento),
                Tema.campo("Código de promoción (opcional)", txtPromocion));
        JPanel contenidoForm = new JPanel(new BorderLayout(0, 18));
        contenidoForm.setOpaque(false);
        contenidoForm.add(form, BorderLayout.NORTH);
        contenidoForm.add(btnCotizar, BorderLayout.SOUTH);
        Tarjeta tarjetaForm = new Tarjeta("Datos de la cotización", contenidoForm);
        tarjetaForm.setPreferredSize(new java.awt.Dimension(390, 0));

        Tema.estilizarArea(txtResultado);
        txtResultado.setLineWrap(true);
        txtResultado.setWrapStyleWord(true);
        txtResultado.setText("Complete los datos y pulse \"Cotizar\" para ver el detalle del cálculo.");

        JLabel etiquetaTotal = new JLabel("TOTAL ESTIMADO");
        etiquetaTotal.setFont(Tema.fuente(Font.BOLD, 12));
        etiquetaTotal.setForeground(new Color(0xC3CEDD));
        lblTotal.setFont(Tema.fuente(Font.BOLD, 28));
        lblTotal.setForeground(Tema.DORADO);
        JPanel totalTextos = new JPanel(new BorderLayout());
        totalTextos.setOpaque(false);
        totalTextos.add(etiquetaTotal, BorderLayout.WEST);
        totalTextos.add(lblTotal, BorderLayout.EAST);
        Tarjeta chipTotal = new Tarjeta(null, totalTextos, Tema.NAVY, 18);

        JPanel contenidoResultado = new JPanel(new BorderLayout(0, 14));
        contenidoResultado.setOpaque(false);
        contenidoResultado.add(Tema.scroll(txtResultado), BorderLayout.CENTER);
        contenidoResultado.add(chipTotal, BorderLayout.SOUTH);

        JPanel cuerpo = Tema.cuerpo();
        cuerpo.setLayout(new BorderLayout(18, 0));
        cuerpo.add(tarjetaForm, BorderLayout.WEST);
        cuerpo.add(new Tarjeta("Detalle del cálculo", contenidoResultado), BorderLayout.CENTER);

        add(new Encabezado("Cotizador de tarifas", "Calcule el precio de una estadía según temporada, canal y descuentos",
                Iconos.COTIZADOR), BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);
        Tema.preparar(this, Iconos.COTIZADOR);

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
        lblTotal.setText(String.format(Locale.US, "S/ %.2f", c.total()));
        txtResultado.setText(sb.toString());
        txtResultado.setCaretPosition(0);
    }
}

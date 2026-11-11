package hotel.vista;

import hotel.canal.Canal;
import hotel.excepcion.AccesoDatosException;
import hotel.excepcion.NegocioException;
import hotel.habitacion.HabitacionLibre;
import hotel.reserva.Reserva;
import hotel.reserva.SolicitudReserva;
import hotel.servicio.ReservaService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

public class DlgNuevaReserva extends JDialog {

    private static final String AUTOMATICA = "Asignación automática";

    private final ReservaService servicio;

    private final JTextField txtDocumento = new JTextField(15);
    private final JComboBox<String> cboTipo;
    private final JComboBox<String> cboCanal = new JComboBox<>();
    private final JTextField txtEntrada = new JTextField(LocalDate.now().plusDays(1).toString());
    private final JTextField txtSalida = new JTextField(LocalDate.now().plusDays(3).toString());
    private final JSpinner spnAdultos = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    private final JSpinner spnNinos = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
    private final JTextField txtPromocion = new JTextField();
    private final JTextField txtObservaciones = new JTextField();
    private final DefaultComboBoxModel<Object> modeloHabitaciones = new DefaultComboBoxModel<>();
    private final JComboBox<Object> cboHabitacion = new JComboBox<>(modeloHabitaciones);

    public DlgNuevaReserva(JFrame propietario, ReservaService servicio) {
        super(propietario, "Nueva reserva", true);
        this.servicio = servicio;
        this.cboTipo = new JComboBox<>(servicio.tiposHabitacion().toArray(new String[0]));
        for (Canal canal : servicio.canales()) {
            cboCanal.addItem(canal.nombre());
        }
        modeloHabitaciones.addElement(AUTOMATICA);

        setSize(520, 470);
        setLocationRelativeTo(propietario);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(10, 2, 5, 5));
        form.setBorder(BorderFactory.createTitledBorder("Datos de la reserva"));
        form.add(new JLabel("Documento del cliente:"));
        form.add(txtDocumento);
        form.add(new JLabel("Tipo de habitación:"));
        form.add(cboTipo);
        form.add(new JLabel("Canal de reserva:"));
        form.add(cboCanal);
        form.add(new JLabel("Entrada (AAAA-MM-DD):"));
        form.add(txtEntrada);
        form.add(new JLabel("Salida (AAAA-MM-DD):"));
        form.add(txtSalida);
        form.add(new JLabel("Adultos:"));
        form.add(spnAdultos);
        form.add(new JLabel("Niños:"));
        form.add(spnNinos);
        form.add(new JLabel("Código de promoción (opcional):"));
        form.add(txtPromocion);
        form.add(new JLabel("Observaciones (opcional):"));
        form.add(txtObservaciones);
        form.add(new JLabel("Habitación:"));
        form.add(cboHabitacion);

        JButton btnBuscar = new JButton("Buscar disponibilidad");
        JButton btnCrear = new JButton("Crear reserva");
        JButton btnCerrar = new JButton("Cerrar");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botones.add(btnBuscar);
        botones.add(btnCrear);
        botones.add(btnCerrar);

        add(form, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> buscarDisponibilidad());
        btnCrear.addActionListener(e -> crear());
        btnCerrar.addActionListener(e -> dispose());
    }

    private void buscarDisponibilidad() {
        try {
            LocalDate entrada = LocalDate.parse(txtEntrada.getText().trim());
            LocalDate salida = LocalDate.parse(txtSalida.getText().trim());
            List<HabitacionLibre> libres = servicio.habitacionesDisponibles(
                    (String) cboTipo.getSelectedItem(), entrada, salida);
            modeloHabitaciones.removeAllElements();
            modeloHabitaciones.addElement(AUTOMATICA);
            for (HabitacionLibre h : libres) {
                modeloHabitaciones.addElement(h);
            }
            if (libres.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No hay habitaciones de ese tipo disponibles en esas fechas.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Hay ").append(libres.size()).append(" habitación(es) disponible(s):\n\n");
            for (HabitacionLibre h : libres) {
                sb.append("  - ").append(h).append('\n');
            }
            sb.append("\nElija una en la lista \"Habitación\" o deje la asignación automática.");
            JOptionPane.showMessageDialog(this, sb.toString(), "Disponibilidad",
                    JOptionPane.INFORMATION_MESSAGE);
            SwingUtilities.invokeLater(cboHabitacion::showPopup);
        } catch (DateTimeParseException ex) {
            avisarFecha();
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void crear() {
        try {
            LocalDate entrada = LocalDate.parse(txtEntrada.getText().trim());
            LocalDate salida = LocalDate.parse(txtSalida.getText().trim());
            Object elegida = cboHabitacion.getSelectedItem();
            Integer idHabitacion = elegida instanceof HabitacionLibre
                    ? ((HabitacionLibre) elegida).id() : null;

            SolicitudReserva solicitud = new SolicitudReserva(
                    txtDocumento.getText(), (String) cboTipo.getSelectedItem(),
                    (String) cboCanal.getSelectedItem(), idHabitacion, entrada, salida,
                    (Integer) spnAdultos.getValue(), (Integer) spnNinos.getValue(),
                    txtPromocion.getText(), txtObservaciones.getText());

            Reserva reserva = servicio.crear(solicitud, null);
            double anticipo = servicio.anticipoRequerido(reserva);

            StringBuilder sb = new StringBuilder();
            sb.append("Reserva ").append(reserva.getCodigo()).append(" creada.\n\n");
            sb.append("Cliente: ").append(reserva.getCliente()).append('\n');
            sb.append("Habitación: ").append(reserva.getHabitacion())
                    .append(" (").append(reserva.getTipo()).append(")\n");
            sb.append("Estado: ").append(reserva.getEstado().nombre()).append('\n');
            sb.append(String.format(Locale.US, "Total: S/ %.2f%n", reserva.getTotal()));
            sb.append(String.format(Locale.US, "Anticipo requerido: S/ %.2f", anticipo));
            JOptionPane.showMessageDialog(this, sb.toString(), "Reserva creada",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (DateTimeParseException ex) {
            avisarFecha();
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void avisarFecha() {
        JOptionPane.showMessageDialog(this, "Use el formato de fecha AAAA-MM-DD.",
                "Validación", JOptionPane.WARNING_MESSAGE);
    }

    private void error(AccesoDatosException ex) {
        String detalle = ex.getCause() != null ? "\n" + ex.getCause().getMessage() : "";
        JOptionPane.showMessageDialog(this, ex.getMessage() + detalle,
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}

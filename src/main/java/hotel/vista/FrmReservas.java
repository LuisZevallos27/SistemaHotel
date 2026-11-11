package hotel.vista;

import hotel.excepcion.AccesoDatosException;
import hotel.excepcion.NegocioException;
import hotel.reserva.RegistroHistorial;
import hotel.reserva.Reserva;
import hotel.servicio.ReservaService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

public class FrmReservas extends JFrame {

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ReservaService servicio;
    private final JComboBox<String> cboFiltro = new JComboBox<>();
    private List<Reserva> reservas = new ArrayList<>();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Código", "Cliente", "Documento", "Tipo", "Hab.", "Canal", "Entrada",
                "Salida", "Noches", "Total (S/)", "Anticipo pagado (S/)", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    public FrmReservas(ReservaService servicio) {
        this.servicio = servicio;

        setTitle("Reservas - Hotel");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1150, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        cboFiltro.addItem("TODOS");
        for (String estado : servicio.estados()) {
            cboFiltro.addItem(estado);
        }

        JButton btnNueva = new JButton("Nueva reserva");
        JButton btnConfirmar = new JButton("Confirmar");
        JButton btnCancelar = new JButton("Cancelar reserva");
        JButton btnNoShow = new JButton("Marcar no-show");
        JButton btnHistorial = new JButton("Ver historial");
        JButton btnActualizar = new JButton("Actualizar");

        JPanel norte = new JPanel(new FlowLayout(FlowLayout.LEFT));
        norte.add(new JLabel("Estado:"));
        norte.add(cboFiltro);
        norte.add(btnActualizar);
        norte.add(btnNueva);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sur.add(btnConfirmar);
        sur.add(btnCancelar);
        sur.add(btnNoShow);
        sur.add(btnHistorial);

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);

        cboFiltro.addActionListener(e -> cargarTabla());
        btnActualizar.addActionListener(e -> cargarTabla());
        btnNueva.addActionListener(e -> nuevaReserva());
        btnConfirmar.addActionListener(e -> confirmar());
        btnCancelar.addActionListener(e -> cancelar());
        btnNoShow.addActionListener(e -> marcarNoShow());
        btnHistorial.addActionListener(e -> verHistorial());

        cargarTabla();
    }

    private void cargarTabla() {
        try {
            reservas = servicio.listar((String) cboFiltro.getSelectedItem());
            modelo.setRowCount(0);
            for (Reserva r : reservas) {
                modelo.addRow(new Object[]{r.getCodigo(), r.getCliente(), r.getDocumento(),
                    r.getTipo(), r.getHabitacion() == null ? "-" : r.getHabitacion(),
                    r.getCanal(), r.getFechaEntrada(), r.getFechaSalida(), r.getNoches(),
                    String.format(Locale.US, "%.2f", r.getTotal()),
                    String.format(Locale.US, "%.2f", r.getAnticipo()),
                    r.getEstado().nombre()});
            }
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private Reserva seleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una reserva de la tabla.");
            return null;
        }
        return reservas.get(fila);
    }

    private void nuevaReserva() {
        new DlgNuevaReserva(this, servicio).setVisible(true);
        cargarTabla();
    }

    private void confirmar() {
        Reserva r = seleccionada();
        if (r == null) {
            return;
        }
        try {
            servicio.confirmar(r, null);
            JOptionPane.showMessageDialog(this, "Reserva " + r.getCodigo() + " confirmada.");
            cargarTabla();
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void cancelar() {
        Reserva r = seleccionada();
        if (r == null) {
            return;
        }
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Cancelar la reserva " + r.getCodigo() + "?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            boolean conPenalidad = servicio.cancelar(r, null);
            JOptionPane.showMessageDialog(this, conPenalidad
                    ? "Reserva cancelada CON penalidad (fuera del plazo de cancelación gratuita del canal)."
                    : "Reserva cancelada sin penalidad.");
            cargarTabla();
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void marcarNoShow() {
        Reserva r = seleccionada();
        if (r == null) {
            return;
        }
        try {
            servicio.marcarNoShow(r, null);
            JOptionPane.showMessageDialog(this, "Reserva " + r.getCodigo() + " marcada como no-show.");
            cargarTabla();
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void verHistorial() {
        Reserva r = seleccionada();
        if (r == null) {
            return;
        }
        try {
            StringBuilder sb = new StringBuilder();
            for (RegistroHistorial h : servicio.historial(r.getId())) {
                sb.append(h.fecha().format(FORMATO_FECHA_HORA)).append(" | ")
                        .append(h.estadoAnterior() == null ? "-" : h.estadoAnterior())
                        .append(" -> ").append(h.estadoNuevo()).append(" | ")
                        .append(h.usuario() == null ? "-" : h.usuario()).append(" | ")
                        .append(h.detalle() == null ? "" : h.detalle()).append('\n');
            }
            JTextArea area = new JTextArea(sb.toString(), 10, 70);
            area.setEditable(false);
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            JOptionPane.showMessageDialog(this, new JScrollPane(area),
                    "Historial de " + r.getCodigo(), JOptionPane.INFORMATION_MESSAGE);
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void error(AccesoDatosException ex) {
        String detalle = ex.getCause() != null ? "\n" + ex.getCause().getMessage() : "";
        JOptionPane.showMessageDialog(this, ex.getMessage() + detalle,
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}

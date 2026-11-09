package hotel.vista;

import hotel.excepcion.AccesoDatosException;
import hotel.excepcion.NegocioException;
import hotel.habitacion.EstadoHabitacion;
import hotel.habitacion.Habitacion;
import hotel.servicio.HabitacionService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class FrmHabitaciones extends JFrame {

    private final HabitacionService servicio;
    private final JComboBox<EstadoHabitacion> cboEstado;
    private List<Habitacion> habitaciones = new ArrayList<>();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Número", "Piso", "Tipo", "Capacidad", "Tarifa base (S/)",
                "Estado", "Check-out", "Desayuno", "Amenidades"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    public FrmHabitaciones(HabitacionService servicio) {
        this.servicio = servicio;
        this.cboEstado = new JComboBox<>(servicio.estadosManuales().toArray(new EstadoHabitacion[0]));

        setTitle("Habitaciones - Hotel");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1000, 460);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JButton btnCambiar = new JButton("Cambiar estado");
        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelEstado.setBorder(BorderFactory.createTitledBorder("Estado de la habitación seleccionada"));
        panelEstado.add(cboEstado);
        panelEstado.add(btnCambiar);

        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(panelEstado, BorderLayout.SOUTH);

        btnCambiar.addActionListener(e -> cambiarEstado());

        cargarTabla();
    }

    private void cargarTabla() {
        try {
            habitaciones = servicio.listar();
            modelo.setRowCount(0);
            for (Habitacion h : habitaciones) {
                modelo.addRow(new Object[]{h.getNumero(), h.getPiso(), h.getTipo(), h.getCapacidad(),
                    String.format(Locale.US, "%.2f", h.getTarifaBase()), h.getEstado(),
                    h.getHoraCheckOut() + ":00", h.incluyeDesayuno() ? "Sí" : "No",
                    String.join(", ", h.getAmenidades())});
            }
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void cambiarEstado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una habitación de la tabla.");
            return;
        }
        try {
            servicio.cambiarEstado(habitaciones.get(fila), (EstadoHabitacion) cboEstado.getSelectedItem());
            cargarTabla();
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
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

package hotel.vista;

import hotel.excepcion.AccesoDatosException;
import hotel.excepcion.NegocioException;
import hotel.modelo.habitacion.EstadoHabitacion;
import hotel.modelo.habitacion.Habitacion;
import hotel.controlador.HabitacionService;
import hotel.vista.estilo.Encabezado;
import hotel.vista.estilo.Iconos;
import hotel.vista.estilo.Tabla;
import hotel.vista.estilo.Tarjeta;
import hotel.vista.estilo.Tema;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
    private final JTable tabla = new Tabla(modelo);

    public FrmHabitaciones(HabitacionService servicio) {
        this.servicio = servicio;
        this.cboEstado = new JComboBox<>(servicio.estadosManuales().toArray(new EstadoHabitacion[0]));

        setTitle("Habitaciones - Hotel");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1120, 640);
        setMinimumSize(new java.awt.Dimension(900, 520));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JButton btnCambiar = Tema.boton("Cambiar estado", Tema.Variante.PRIMARIO);

        JPanel tabla2 = new JPanel(new BorderLayout(0, 8));
        tabla2.setOpaque(false);
        tabla2.add(Tema.scroll(tabla), BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.add(Tema.contador(modelo), BorderLayout.EAST);
        tabla2.add(pie, BorderLayout.SOUTH);

        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panelEstado.setOpaque(false);
        panelEstado.add(Tema.etiqueta("Nuevo estado:"));
        panelEstado.add(cboEstado);
        panelEstado.add(btnCambiar);

        JPanel cuerpo = Tema.cuerpo();
        cuerpo.add(new Tarjeta("Inventario de habitaciones", tabla2), BorderLayout.CENTER);
        cuerpo.add(new Tarjeta("Estado de la habitación seleccionada", panelEstado), BorderLayout.SOUTH);

        add(new Encabezado("Habitaciones", "Consulte el inventario y cambie el estado de cada habitación",
                Iconos.HABITACIONES), BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);
        Tema.preparar(this, Iconos.HABITACIONES);

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

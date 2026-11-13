package hotel.vista;

import hotel.excepcion.AccesoDatosException;
import hotel.excepcion.NegocioException;
import hotel.habitacion.Habitacion;
import hotel.operacion.Empleado;
import hotel.operacion.EstadoTarea;
import hotel.operacion.Mantenimiento;
import hotel.operacion.Prioridad;
import hotel.operacion.TareaLimpieza;
import hotel.servicio.HabitacionService;
import hotel.servicio.LimpiezaService;
import hotel.servicio.MantenimientoService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class FrmOperaciones extends JFrame {

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final LimpiezaService limpieza;
    private final MantenimientoService mantenimiento;
    private final HabitacionService habitaciones;
    private final Integer idUsuario;

    private List<TareaLimpieza> tareas = new ArrayList<>();
    private List<Mantenimiento> reportes = new ArrayList<>();

    private final JComboBox<String> cboFiltroTareas = new JComboBox<>();
    private final DefaultTableModel modeloTareas = crearModelo(new String[]{
        "Habitación", "Tipo", "Estado", "Asignado a", "Creada"});
    private final JTable tablaTareas = new JTable(modeloTareas);

    private final JComboBox<String> cboFiltroReportes = new JComboBox<>();
    private final DefaultTableModel modeloReportes = crearModelo(new String[]{
        "Habitación", "Descripción", "Prioridad", "Estado", "Reportado", "Cerrado", "Reportó"});
    private final JTable tablaReportes = new JTable(modeloReportes);

    public FrmOperaciones(LimpiezaService limpieza, MantenimientoService mantenimiento,
            HabitacionService habitaciones, Integer idUsuario) {
        this.limpieza = limpieza;
        this.mantenimiento = mantenimiento;
        this.habitaciones = habitaciones;
        this.idUsuario = idUsuario;

        setTitle("Operaciones - Limpieza y mantenimiento");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1000, 520);
        setLocationRelativeTo(null);

        cboFiltroTareas.addItem("TODAS");
        for (String estado : limpieza.estados()) {
            cboFiltroTareas.addItem(estado);
        }
        cboFiltroReportes.addItem("TODOS");
        for (String estado : mantenimiento.estados()) {
            cboFiltroReportes.addItem(estado);
        }

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Limpieza", panelLimpieza());
        pestanas.addTab("Mantenimiento", panelMantenimiento());
        add(pestanas);

        cargarTareas();
        cargarReportes();
    }

    private JPanel panelLimpieza() {
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnAsignar = new JButton("Asignar a...");
        JButton btnAvanzar = new JButton("Avanzar estado");

        JPanel norte = new JPanel(new FlowLayout(FlowLayout.LEFT));
        norte.add(new JLabel("Estado:"));
        norte.add(cboFiltroTareas);
        norte.add(btnActualizar);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sur.add(btnAsignar);
        sur.add(btnAvanzar);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(norte, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaTareas), BorderLayout.CENTER);
        panel.add(sur, BorderLayout.SOUTH);

        cboFiltroTareas.addActionListener(e -> cargarTareas());
        btnActualizar.addActionListener(e -> cargarTareas());
        btnAsignar.addActionListener(e -> asignarTarea());
        btnAvanzar.addActionListener(e -> avanzarTarea());
        return panel;
    }

    private JPanel panelMantenimiento() {
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnReportar = new JButton("Reportar daño");
        JButton btnCerrar = new JButton("Cerrar reporte");

        JPanel norte = new JPanel(new FlowLayout(FlowLayout.LEFT));
        norte.add(new JLabel("Estado:"));
        norte.add(cboFiltroReportes);
        norte.add(btnActualizar);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sur.add(btnReportar);
        sur.add(btnCerrar);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(norte, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaReportes), BorderLayout.CENTER);
        panel.add(sur, BorderLayout.SOUTH);

        cboFiltroReportes.addActionListener(e -> cargarReportes());
        btnActualizar.addActionListener(e -> cargarReportes());
        btnReportar.addActionListener(e -> reportarDano());
        btnCerrar.addActionListener(e -> cerrarReporte());
        return panel;
    }

    private DefaultTableModel crearModelo(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private void cargarTareas() {
        try {
            tareas = limpieza.tareas((String) cboFiltroTareas.getSelectedItem());
            modeloTareas.setRowCount(0);
            for (TareaLimpieza t : tareas) {
                modeloTareas.addRow(new Object[]{t.habitacion(), t.tipo(), t.estado(),
                    t.asignado() == null ? "-" : t.asignado(), texto(t.fechaCreacion())});
            }
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void cargarReportes() {
        try {
            reportes = mantenimiento.listar((String) cboFiltroReportes.getSelectedItem());
            modeloReportes.setRowCount(0);
            for (Mantenimiento m : reportes) {
                modeloReportes.addRow(new Object[]{m.habitacion(), m.descripcion(), m.prioridad(),
                    m.estado(), texto(m.fechaReporte()), texto(m.fechaCierre()),
                    m.reporta() == null ? "-" : m.reporta()});
            }
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void asignarTarea() {
        int fila = tablaTareas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una tarea de la tabla.");
            return;
        }
        try {
            List<Empleado> personal = limpieza.personal();
            if (personal.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay personal de housekeeping activo.",
                        "Sin personal", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Object elegido = JOptionPane.showInputDialog(this, "Asignar a:", "Asignar tarea",
                    JOptionPane.QUESTION_MESSAGE, null, personal.toArray(), personal.get(0));
            if (elegido == null) {
                return;
            }
            limpieza.asignar(tareas.get(fila), (Empleado) elegido);
            cargarTareas();
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void avanzarTarea() {
        int fila = tablaTareas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una tarea de la tabla.");
            return;
        }
        try {
            EstadoTarea nuevo = limpieza.avanzar(tareas.get(fila));
            JOptionPane.showMessageDialog(this, nuevo.esFinal()
                    ? "Tarea terminada. Si la habitación estaba en limpieza, quedó disponible."
                    : "La tarea pasó a " + nuevo + ".");
            cargarTareas();
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void reportarDano() {
        try {
            List<Habitacion> lista = habitaciones.listar();
            String[] etiquetas = new String[lista.size()];
            for (int i = 0; i < lista.size(); i++) {
                Habitacion h = lista.get(i);
                etiquetas[i] = h.getNumero() + " - " + h.getTipo() + " (" + h.getEstado() + ")";
            }
            JComboBox<String> cboHabitacion = new JComboBox<>(etiquetas);
            JComboBox<Prioridad> cboPrioridad = new JComboBox<>(Prioridad.values());
            cboPrioridad.setSelectedItem(Prioridad.MEDIA);
            JTextField txtDescripcion = new JTextField(30);

            JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
            panel.add(new JLabel("Habitación:"));
            panel.add(cboHabitacion);
            panel.add(new JLabel("Prioridad:"));
            panel.add(cboPrioridad);
            panel.add(new JLabel("Descripción del daño:"));
            panel.add(txtDescripcion);

            int opcion = JOptionPane.showConfirmDialog(this, panel, "Reportar daño",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcion != JOptionPane.OK_OPTION) {
                return;
            }
            mantenimiento.reportar(lista.get(cboHabitacion.getSelectedIndex()),
                    txtDescripcion.getText(), (Prioridad) cboPrioridad.getSelectedItem(), idUsuario);
            JOptionPane.showMessageDialog(this, "Reporte registrado.");
            cargarReportes();
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void cerrarReporte() {
        int fila = tablaReportes.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un reporte de la tabla.");
            return;
        }
        try {
            mantenimiento.cerrar(reportes.get(fila));
            JOptionPane.showMessageDialog(this, "Reporte cerrado.");
            cargarReportes();
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private String texto(LocalDateTime fecha) {
        return fecha == null ? "-" : fecha.format(FORMATO_FECHA_HORA);
    }

    private void error(AccesoDatosException ex) {
        String detalle = ex.getCause() != null ? "\n" + ex.getCause().getMessage() : "";
        JOptionPane.showMessageDialog(this, ex.getMessage() + detalle,
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}

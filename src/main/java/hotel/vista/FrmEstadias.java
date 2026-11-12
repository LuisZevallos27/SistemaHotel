package hotel.vista;

import hotel.estadia.Cuenta;
import hotel.estadia.ElementoCuenta;
import hotel.estadia.Estadia;
import hotel.estadia.Servicio;
import hotel.excepcion.AccesoDatosException;
import hotel.excepcion.NegocioException;
import hotel.habitacion.HabitacionLibre;
import hotel.reserva.Reserva;
import hotel.servicio.EstadiaService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

public class FrmEstadias extends JFrame {

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int ANCHO_DESCRIPCION = 48;

    private final EstadiaService servicio;
    private List<Reserva> llegadas = new ArrayList<>();
    private List<Estadia> estadias = new ArrayList<>();

    private final DefaultTableModel modeloLlegadas = crearModelo(new String[]{
        "Código", "Cliente", "Documento", "Tipo", "Hab. reservada", "Entrada", "Salida", "Canal", "Total (S/)"});
    private final JTable tablaLlegadas = new JTable(modeloLlegadas);

    private final DefaultTableModel modeloEstadias = crearModelo(new String[]{
        "Reserva", "Cliente", "Documento", "Habitación", "Check-in", "Salida prevista", "Alojamiento (S/)"});
    private final JTable tablaEstadias = new JTable(modeloEstadias);

    public FrmEstadias(EstadiaService servicio) {
        this.servicio = servicio;

        setTitle("Estadías - Hotel");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1050, 520);
        setLocationRelativeTo(null);

        JButton btnCheckIn = new JButton("Hacer check-in");
        JPanel panelLlegadas = new JPanel(new BorderLayout(5, 5));
        panelLlegadas.add(new JScrollPane(tablaLlegadas), BorderLayout.CENTER);
        JPanel botonesLlegadas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botonesLlegadas.add(btnCheckIn);
        panelLlegadas.add(botonesLlegadas, BorderLayout.SOUTH);

        JButton btnConsumo = new JButton("Registrar consumo");
        JButton btnCuenta = new JButton("Ver cuenta");
        JPanel panelEstadias = new JPanel(new BorderLayout(5, 5));
        panelEstadias.add(new JScrollPane(tablaEstadias), BorderLayout.CENTER);
        JPanel botonesEstadias = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botonesEstadias.add(btnConsumo);
        botonesEstadias.add(btnCuenta);
        panelEstadias.add(botonesEstadias, BorderLayout.SOUTH);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Llegadas de hoy (check-in)", panelLlegadas);
        pestanas.addTab("Estadías en curso", panelEstadias);
        add(pestanas);

        btnCheckIn.addActionListener(e -> hacerCheckIn());
        btnConsumo.addActionListener(e -> registrarConsumo());
        btnCuenta.addActionListener(e -> verCuenta());

        cargarTablas();
    }

    private DefaultTableModel crearModelo(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private void cargarTablas() {
        try {
            llegadas = servicio.llegadasDeHoy();
            modeloLlegadas.setRowCount(0);
            for (Reserva r : llegadas) {
                modeloLlegadas.addRow(new Object[]{r.getCodigo(), r.getCliente(), r.getDocumento(),
                    r.getTipo(), r.getHabitacion() == null ? "-" : r.getHabitacion(),
                    r.getFechaEntrada(), r.getFechaSalida(), r.getCanal(),
                    String.format(Locale.US, "%.2f", r.getTotal())});
            }
            estadias = servicio.estadiasEnCurso();
            modeloEstadias.setRowCount(0);
            for (Estadia e : estadias) {
                modeloEstadias.addRow(new Object[]{e.codigoReserva(), e.cliente(), e.documento(),
                    e.habitacion(), e.fechaCheckIn().format(FORMATO_FECHA_HORA), e.fechaSalida(),
                    String.format(Locale.US, "%.2f", e.totalAlojamiento())});
            }
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void hacerCheckIn() {
        int fila = tablaLlegadas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una reserva de la tabla.");
            return;
        }
        Reserva reserva = llegadas.get(fila);
        try {
            List<HabitacionLibre> opciones = servicio.habitacionesParaCheckIn(reserva);
            if (opciones.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No hay habitaciones listas (disponibles) para este check-in.",
                        "Sin habitaciones", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Object elegida = JOptionPane.showInputDialog(this,
                    "Habitación para " + reserva.getCliente() + ":", "Check-in " + reserva.getCodigo(),
                    JOptionPane.QUESTION_MESSAGE, null, opciones.toArray(), opciones.get(0));
            if (elegida == null) {
                return;
            }
            servicio.registrarCheckIn(reserva, ((HabitacionLibre) elegida).id(), null);
            JOptionPane.showMessageDialog(this, "Check-in realizado en la " + elegida + ".");
            cargarTablas();
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (AccesoDatosException ex) {
            error(ex);
            cargarTablas();
        }
    }

    private Estadia estadiaSeleccionada() {
        int fila = tablaEstadias.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una estadía de la tabla.");
            return null;
        }
        return estadias.get(fila);
    }

    private void registrarConsumo() {
        Estadia estadia = estadiaSeleccionada();
        if (estadia == null) {
            return;
        }
        try {
            JComboBox<Servicio> cboServicio = new JComboBox<>(servicio.servicios().toArray(new Servicio[0]));
            JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
            JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
            panel.add(new JLabel("Servicio:"));
            panel.add(cboServicio);
            panel.add(new JLabel("Cantidad:"));
            panel.add(spnCantidad);

            int opcion = JOptionPane.showConfirmDialog(this, panel,
                    "Consumo de " + estadia.cliente() + " (Hab. " + estadia.habitacion() + ")",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcion != JOptionPane.OK_OPTION) {
                return;
            }
            servicio.registrarConsumo(estadia, (Servicio) cboServicio.getSelectedItem(),
                    (Integer) spnCantidad.getValue(), null);
            JOptionPane.showMessageDialog(this, "Consumo registrado.");
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void verCuenta() {
        Estadia estadia = estadiaSeleccionada();
        if (estadia == null) {
            return;
        }
        try {
            Cuenta cuenta = servicio.cuentaDe(estadia);
            StringBuilder sb = new StringBuilder();
            dibujar(cuenta.detalle(), 0, sb);
            sb.append('\n');
            sb.append(String.format(Locale.US, "Anticipos pagados: S/ %.2f%n", cuenta.anticipos()));
            sb.append(String.format(Locale.US, "SALDO A PAGAR:    S/ %.2f", cuenta.saldo()));

            JTextArea area = new JTextArea(sb.toString(), 16, 66);
            area.setEditable(false);
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            JOptionPane.showMessageDialog(this, new JScrollPane(area),
                    "Cuenta de " + estadia.cliente(), JOptionPane.INFORMATION_MESSAGE);
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void dibujar(ElementoCuenta elemento, int nivel, StringBuilder sb) {
        String texto = "  ".repeat(nivel) + elemento.descripcion();
        String relleno = ".".repeat(Math.max(2, ANCHO_DESCRIPCION - texto.length()));
        sb.append(texto).append(' ').append(relleno)
                .append(String.format(Locale.US, " S/ %9.2f%n", elemento.total()));
        for (ElementoCuenta hijo : elemento.hijos()) {
            dibujar(hijo, nivel + 1, sb);
        }
    }

    private void error(AccesoDatosException ex) {
        String detalle = ex.getCause() != null ? "\n" + ex.getCause().getMessage() : "";
        JOptionPane.showMessageDialog(this, ex.getMessage() + detalle,
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}

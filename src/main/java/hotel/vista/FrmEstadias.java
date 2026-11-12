package hotel.vista;

import hotel.estadia.Cuenta;
import hotel.estadia.ElementoCuenta;
import hotel.estadia.Estadia;
import hotel.estadia.Servicio;
import hotel.excepcion.AccesoDatosException;
import hotel.excepcion.NegocioException;
import hotel.facturacion.Factura;
import hotel.facturacion.LineaFactura;
import hotel.facturacion.MetodoPago;
import hotel.facturacion.ResultadoCheckOut;
import hotel.facturacion.TipoComprobante;
import hotel.habitacion.HabitacionLibre;
import hotel.reserva.Reserva;
import hotel.servicio.CheckOutService;
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
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

public class FrmEstadias extends JFrame {

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int ANCHO_DESCRIPCION = 48;
    private static final int ANCHO_COMPROBANTE = 62;

    private final EstadiaService servicio;
    private final CheckOutService checkOut;
    private List<Reserva> llegadas = new ArrayList<>();
    private List<Estadia> estadias = new ArrayList<>();

    private final DefaultTableModel modeloLlegadas = crearModelo(new String[]{
        "Código", "Cliente", "Documento", "Tipo", "Hab. reservada", "Entrada", "Salida", "Canal", "Total (S/)"});
    private final JTable tablaLlegadas = new JTable(modeloLlegadas);

    private final DefaultTableModel modeloEstadias = crearModelo(new String[]{
        "Reserva", "Cliente", "Documento", "Habitación", "Check-in", "Salida prevista", "Alojamiento (S/)"});
    private final JTable tablaEstadias = new JTable(modeloEstadias);

    public FrmEstadias(EstadiaService servicio, CheckOutService checkOut) {
        this.servicio = servicio;
        this.checkOut = checkOut;

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
        JButton btnCheckOut = new JButton("Hacer check-out");
        JPanel panelEstadias = new JPanel(new BorderLayout(5, 5));
        panelEstadias.add(new JScrollPane(tablaEstadias), BorderLayout.CENTER);
        JPanel botonesEstadias = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botonesEstadias.add(btnConsumo);
        botonesEstadias.add(btnCuenta);
        botonesEstadias.add(btnCheckOut);
        panelEstadias.add(botonesEstadias, BorderLayout.SOUTH);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Llegadas de hoy (check-in)", panelLlegadas);
        pestanas.addTab("Estadías en curso", panelEstadias);
        add(pestanas);

        btnCheckIn.addActionListener(e -> hacerCheckIn());
        btnConsumo.addActionListener(e -> registrarConsumo());
        btnCuenta.addActionListener(e -> verCuenta());
        btnCheckOut.addActionListener(e -> hacerCheckOut());

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
            mostrarTexto(sb.toString(), "Cuenta de " + estadia.cliente(), 16, 66);
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void hacerCheckOut() {
        Estadia estadia = estadiaSeleccionada();
        if (estadia == null) {
            return;
        }
        try {
            Cuenta cuenta = servicio.cuentaDe(estadia);
            JComboBox<TipoComprobante> cboComprobante = new JComboBox<>(TipoComprobante.values());
            JComboBox<MetodoPago> cboMetodo = new JComboBox<>(MetodoPago.values());
            JTextField txtReferencia = new JTextField();

            JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
            panel.add(new JLabel("Total de la cuenta:"));
            panel.add(new JLabel(String.format(Locale.US, "S/ %.2f", cuenta.total())));
            panel.add(new JLabel("Anticipos pagados:"));
            panel.add(new JLabel(String.format(Locale.US, "S/ %.2f", cuenta.anticipos())));
            panel.add(new JLabel("Saldo a pagar:"));
            panel.add(new JLabel(String.format(Locale.US, "S/ %.2f", cuenta.saldo())));
            panel.add(new JLabel("Comprobante:"));
            panel.add(cboComprobante);
            panel.add(new JLabel("Método de pago:"));
            panel.add(cboMetodo);
            panel.add(new JLabel("Referencia (opcional):"));
            panel.add(txtReferencia);

            int opcion = JOptionPane.showConfirmDialog(this, panel,
                    "Check-out de " + estadia.cliente() + " (Hab. " + estadia.habitacion() + ")",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcion != JOptionPane.OK_OPTION) {
                return;
            }
            ResultadoCheckOut resultado = checkOut.finalizar(estadia,
                    (TipoComprobante) cboComprobante.getSelectedItem(),
                    (MetodoPago) cboMetodo.getSelectedItem(), txtReferencia.getText(), null);
            mostrarTexto(comprobante(estadia, resultado), "Comprobante emitido", 24, 66);
            cargarTablas();
            pedirEncuesta(estadia);
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (AccesoDatosException ex) {
            error(ex);
            cargarTablas();
        }
    }

    private void pedirEncuesta(Estadia estadia) {
        Object puntaje = JOptionPane.showInputDialog(this,
                "Satisfacción del huésped (1 a 5). Cancele para omitir:", "Encuesta de satisfacción",
                JOptionPane.QUESTION_MESSAGE, null, new Integer[]{5, 4, 3, 2, 1}, 5);
        if (puntaje == null) {
            return;
        }
        try {
            checkOut.registrarEncuesta(estadia, (Integer) puntaje, null);
            JOptionPane.showMessageDialog(this, "Gracias. Encuesta registrada.");
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private String comprobante(Estadia estadia, ResultadoCheckOut resultado) {
        Factura f = resultado.factura();
        String doble = "=".repeat(ANCHO_COMPROBANTE);
        String simple = "-".repeat(ANCHO_COMPROBANTE);
        StringBuilder sb = new StringBuilder();
        sb.append(doble).append('\n');
        sb.append(f.tipo()).append(' ').append(f.numeroCompleto()).append('\n');
        sb.append("Fecha: ").append(f.fechaEmision().format(FORMATO_FECHA_HORA)).append('\n');
        sb.append("Cliente: ").append(f.cliente()).append(" (").append(f.documento()).append(")\n");
        if (f.empresa() != null) {
            sb.append("Empresa: ").append(f.empresa()).append(" - RUC ").append(f.ruc()).append('\n');
        }
        sb.append("Habitación: ").append(estadia.habitacion()).append('\n');
        sb.append(simple).append('\n');
        for (LineaFactura linea : resultado.lineas()) {
            sb.append(fila(linea.concepto(), linea.subtotal()));
        }
        sb.append(simple).append('\n');
        sb.append(fila("Subtotal (sin IGV)", f.subtotal()));
        sb.append(fila("IGV 18%", f.igv()));
        sb.append(fila("TOTAL", f.total()));
        sb.append(fila("Anticipos aplicados", resultado.anticipos()));
        sb.append(fila("Pagado en el check-out", resultado.montoPagado()));
        sb.append("Estado: ").append(f.estado()).append('\n');
        sb.append(doble);
        return sb.toString();
    }

    private String fila(String texto, double monto) {
        String relleno = ".".repeat(Math.max(2, ANCHO_DESCRIPCION - texto.length()));
        return texto + " " + relleno + String.format(Locale.US, " S/ %9.2f%n", monto);
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

    private void mostrarTexto(String texto, String titulo, int filas, int columnas) {
        JTextArea area = new JTextArea(texto, filas, columnas);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), titulo,
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(AccesoDatosException ex) {
        String detalle = ex.getCause() != null ? "\n" + ex.getCause().getMessage() : "";
        JOptionPane.showMessageDialog(this, ex.getMessage() + detalle,
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}

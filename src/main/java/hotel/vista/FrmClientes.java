package hotel.vista;

import hotel.modelo.Cliente;
import hotel.excepcion.AccesoDatosException;
import hotel.controlador.ClienteService;
import hotel.excepcion.NegocioException;
import hotel.vista.estilo.Encabezado;
import hotel.vista.estilo.Iconos;
import hotel.vista.estilo.Tabla;
import hotel.vista.estilo.Tarjeta;
import hotel.vista.estilo.Tema;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class FrmClientes extends JFrame {

    private final ClienteService servicio;

    private final JComboBox<String> cboTipoDoc;
    private final JTextField txtDocumento = new JTextField(15);
    private final JTextField txtNombres = new JTextField(15);
    private final JTextField txtApellidos = new JTextField(15);
    private final JTextField txtTelefono = new JTextField(15);
    private final JTextField txtCorreo = new JTextField(15);
    private final JTextField txtNacionalidad = new JTextField(15);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Tipo", "Documento", "Nombres", "Apellidos",
                "Teléfono", "Correo", "Nacionalidad"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new Tabla(modelo);
    private int idSeleccionado = 0;

    public FrmClientes(ClienteService servicio) {
        this.servicio = servicio;
        this.cboTipoDoc = new JComboBox<>(servicio.tiposDocumento().toArray(new String[0]));

        setTitle("Gestión de Clientes - Hotel");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1000, 780);
        setMinimumSize(new java.awt.Dimension(860, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JButton btnNuevo = Tema.boton("Nuevo", Tema.Variante.SECUNDARIO);
        JButton btnGuardar = Tema.boton("Guardar", Tema.Variante.PRIMARIO);
        JButton btnEliminar = Tema.boton("Eliminar", Tema.Variante.PELIGRO);

        JPanel formulario = Tema.apilar(14,
                Tema.fila(new int[]{2, 3, 3, 3},
                        Tema.campo("Tipo de documento", cboTipoDoc),
                        Tema.campo("Documento", txtDocumento),
                        Tema.campo("Nombres", txtNombres),
                        Tema.campo("Apellidos", txtApellidos)),
                Tema.fila(new int[]{3, 4, 3},
                        Tema.campo("Teléfono", txtTelefono),
                        Tema.campo("Correo electrónico", txtCorreo),
                        Tema.campo("Nacionalidad", txtNacionalidad)));

        JPanel botones = Tema.barra(FlowLayout.RIGHT, btnNuevo, btnEliminar, btnGuardar);
        JPanel contenidoForm = new JPanel(new BorderLayout(0, 16));
        contenidoForm.setOpaque(false);
        contenidoForm.add(formulario, BorderLayout.CENTER);
        contenidoForm.add(botones, BorderLayout.SOUTH);

        JPanel contenidoTabla = new JPanel(new BorderLayout(0, 8));
        contenidoTabla.setOpaque(false);
        contenidoTabla.add(Tema.scroll(tabla), BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.add(Tema.contador(modelo), BorderLayout.EAST);
        contenidoTabla.add(pie, BorderLayout.SOUTH);

        JPanel cuerpo = Tema.cuerpo();
        cuerpo.add(new Tarjeta("Datos del cliente", contenidoForm), BorderLayout.NORTH);
        cuerpo.add(new Tarjeta("Listado de clientes", contenidoTabla), BorderLayout.CENTER);

        add(new Encabezado("Clientes", "Registre, edite y consulte a los huéspedes del hotel",
                Iconos.CLIENTES), BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);
        Tema.preparar(this, Iconos.CLIENTES);

        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });

        cargarTabla();
    }

    private void cargarTabla() {
        try {
            modelo.setRowCount(0);
            for (Cliente c : servicio.listar()) {
                modelo.addRow(new Object[]{c.getIdCliente(), c.getTipoDocumento(),
                    c.getDocumento(), c.getNombres(), c.getApellidos(),
                    c.getTelefono(), c.getCorreo(), c.getNacionalidad()});
            }
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        idSeleccionado = (int) modelo.getValueAt(fila, 0);
        cboTipoDoc.setSelectedItem(modelo.getValueAt(fila, 1));
        txtDocumento.setText(texto(modelo.getValueAt(fila, 2)));
        txtNombres.setText(texto(modelo.getValueAt(fila, 3)));
        txtApellidos.setText(texto(modelo.getValueAt(fila, 4)));
        txtTelefono.setText(texto(modelo.getValueAt(fila, 5)));
        txtCorreo.setText(texto(modelo.getValueAt(fila, 6)));
        txtNacionalidad.setText(texto(modelo.getValueAt(fila, 7)));
    }

    private void guardar() {
        Cliente c = new Cliente();
        c.setIdCliente(idSeleccionado);
        c.setTipoDocumento((String) cboTipoDoc.getSelectedItem());
        c.setDocumento(txtDocumento.getText().trim());
        c.setNombres(txtNombres.getText().trim());
        c.setApellidos(txtApellidos.getText().trim());
        c.setTelefono(txtTelefono.getText().trim());
        c.setCorreo(txtCorreo.getText().trim());
        c.setNacionalidad(txtNacionalidad.getText().trim());
        try {
            servicio.guardar(c);
            JOptionPane.showMessageDialog(this, "Cliente guardado correctamente.");
            limpiar();
            cargarTabla();
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void eliminar() {
        if (idSeleccionado == 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "¿Eliminar el cliente seleccionado?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.desactivar(idSeleccionado);
            limpiar();
            cargarTabla();
        } catch (AccesoDatosException ex) {
            error(ex);
        }
    }

    private void limpiar() {
        idSeleccionado = 0;
        cboTipoDoc.setSelectedIndex(0);
        txtDocumento.setText("");
        txtNombres.setText("");
        txtApellidos.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtNacionalidad.setText("");
        tabla.clearSelection();
    }

    private String texto(Object o) {
        return o == null ? "" : o.toString();
    }

    private void error(AccesoDatosException ex) {
        String detalle = ex.getCause() != null ? "\n" + ex.getCause().getMessage() : "";
        JOptionPane.showMessageDialog(this, ex.getMessage() + detalle,
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}

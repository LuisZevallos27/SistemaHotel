package hotel.vista;

import hotel.dao.ClienteDAOImpl;
import hotel.modelo.Cliente;
import hotel.servicio.ClienteService;
import hotel.servicio.NegocioException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class FrmClientes extends JFrame {

    private final ClienteService servicio = new ClienteService(new ClienteDAOImpl());

    private final JComboBox<String> cboTipoDoc
            = new JComboBox<>(new String[]{"DNI", "CE", "PASAPORTE", "RUC"});
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
    private final JTable tabla = new JTable(modelo);
    private int idSeleccionado = 0;

    public FrmClientes() {
        setTitle("Gestión de Clientes - Hotel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));
        form.setBorder(BorderFactory.createTitledBorder("Datos del cliente"));
        form.add(new JLabel("Tipo de documento:"));
        form.add(cboTipoDoc);
        form.add(new JLabel("Documento:"));
        form.add(txtDocumento);
        form.add(new JLabel("Nombres:"));
        form.add(txtNombres);
        form.add(new JLabel("Apellidos:"));
        form.add(txtApellidos);
        form.add(new JLabel("Teléfono:"));
        form.add(txtTelefono);
        form.add(new JLabel("Correo:"));
        form.add(txtCorreo);

        JPanel nac = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nac.add(new JLabel("Nacionalidad:"));
        nac.add(txtNacionalidad);

        JButton btnNuevo = new JButton("Nuevo");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnEliminar = new JButton("Eliminar");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnEliminar);

        JPanel centroNorte = new JPanel(new BorderLayout());
        centroNorte.add(form, BorderLayout.CENTER);
        centroNorte.add(nac, BorderLayout.SOUTH);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(centroNorte, BorderLayout.CENTER);
        norte.add(botones, BorderLayout.SOUTH);

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

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
        } catch (SQLException ex) {
            error("Error de base de datos: " + ex.getMessage());
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
        } catch (SQLException ex) {
            error("Error de base de datos: " + ex.getMessage());
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
        } catch (SQLException ex) {
            error("Error de base de datos: " + ex.getMessage());
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

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}

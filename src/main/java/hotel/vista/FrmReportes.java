package hotel.vista;

import hotel.controlador.ReporteService;
import hotel.excepcion.AccesoDatosException;
import hotel.excepcion.NegocioException;
import hotel.modelo.reporte.DefinicionReporte;
import hotel.modelo.reporte.ParametroReporte;
import hotel.modelo.reporte.TipoParametro;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FrmReportes extends JFrame {

    private final ReporteService servicio;
    private final String generadoPor;
    private final JList<DefinicionReporte> lista;
    private final JLabel lblDescripcion = new JLabel(" ");
    private final JPanel panelParametros = new JPanel(new GridLayout(0, 2, 5, 5));
    private final JTextArea areaReporte = new JTextArea();
    private final JButton btnExportar = new JButton("Exportar a TXT");
    private final List<JTextField> campos = new ArrayList<>();

    private String ultimoReporte;
    private DefinicionReporte ultimaDefinicion;

    public FrmReportes(ReporteService servicio, String generadoPor) {
        this.servicio = servicio;
        this.generadoPor = generadoPor;

        DefaultListModel<DefinicionReporte> modelo = new DefaultListModel<>();
        for (DefinicionReporte definicion : servicio.reportes()) {
            modelo.addElement(definicion);
        }
        this.lista = new JList<>(modelo);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setTitle("Reportes gerenciales - Hotel");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JScrollPane scrollLista = new JScrollPane(lista);
        scrollLista.setPreferredSize(new Dimension(260, 0));
        scrollLista.setBorder(BorderFactory.createTitledBorder("Reportes disponibles"));
        add(scrollLista, BorderLayout.WEST);

        JButton btnGenerar = new JButton("Generar reporte");
        btnExportar.setEnabled(false);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnGenerar);
        botones.add(btnExportar);

        JPanel configuracion = new JPanel(new BorderLayout(5, 5));
        configuracion.setBorder(BorderFactory.createTitledBorder("Parámetros"));
        configuracion.add(lblDescripcion, BorderLayout.NORTH);
        configuracion.add(panelParametros, BorderLayout.CENTER);
        configuracion.add(botones, BorderLayout.SOUTH);

        areaReporte.setEditable(false);
        areaReporte.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        areaReporte.setText("Seleccione un reporte y pulse \"Generar reporte\".");

        JPanel derecha = new JPanel(new BorderLayout(5, 5));
        derecha.add(configuracion, BorderLayout.NORTH);
        derecha.add(new JScrollPane(areaReporte), BorderLayout.CENTER);
        add(derecha, BorderLayout.CENTER);

        lista.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarParametros();
            }
        });
        btnGenerar.addActionListener(e -> generar());
        btnExportar.addActionListener(e -> exportar());

        if (modelo.size() > 0) {
            lista.setSelectedIndex(0);
        }
    }

    private void mostrarParametros() {
        panelParametros.removeAll();
        campos.clear();
        DefinicionReporte definicion = lista.getSelectedValue();
        if (definicion != null) {
            lblDescripcion.setText("<html>" + definicion.descripcion() + "</html>");
            for (ParametroReporte parametro : definicion.parametros()) {
                panelParametros.add(new JLabel(etiqueta(parametro)));
                JTextField campo = new JTextField(parametro.valorInicial());
                campos.add(campo);
                panelParametros.add(campo);
            }
        }
        panelParametros.revalidate();
        panelParametros.repaint();
    }

    private String etiqueta(ParametroReporte parametro) {
        String texto = parametro.etiqueta();
        if (parametro.tipo() == TipoParametro.FECHA) {
            texto += " (AAAA-MM-DD)";
        }
        if (!parametro.obligatorio()) {
            texto += " - opcional";
        }
        return texto + ":";
    }

    private void generar() {
        DefinicionReporte definicion = lista.getSelectedValue();
        if (definicion == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un reporte de la lista.");
            return;
        }
        List<String> valores = new ArrayList<>();
        for (JTextField campo : campos) {
            valores.add(campo.getText());
        }
        try {
            String texto = servicio.generar(definicion, valores, generadoPor);
            areaReporte.setText(texto);
            areaReporte.setCaretPosition(0);
            ultimoReporte = texto;
            ultimaDefinicion = definicion;
            btnExportar.setEnabled(true);
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (AccesoDatosException ex) {
            String detalle = ex.getCause() != null ? "\n" + ex.getCause().getMessage() : "";
            JOptionPane.showMessageDialog(this, ex.getMessage() + detalle,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportar() {
        if (ultimoReporte == null || ultimaDefinicion == null) {
            JOptionPane.showMessageDialog(this, "Genere un reporte antes de exportarlo.");
            return;
        }
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Exportar reporte a TXT");
        selector.setFileFilter(new FileNameExtensionFilter("Archivo de texto (*.txt)", "txt"));
        selector.setSelectedFile(new File(servicio.nombreArchivoSugerido(ultimaDefinicion)));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path destino = servicio.destinoTxt(selector.getSelectedFile().toPath());
        if (destino.toFile().exists()) {
            int opcion = JOptionPane.showConfirmDialog(this,
                    "El archivo ya existe:\n" + destino + "\n¿Desea reemplazarlo?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (opcion != JOptionPane.YES_OPTION) {
                return;
            }
        }
        try {
            servicio.exportar(ultimoReporte, destino);
            JOptionPane.showMessageDialog(this, "Reporte exportado en:\n" + destino.toAbsolutePath(),
                    "Exportación completa", JOptionPane.INFORMATION_MESSAGE);
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "No se pudo exportar", JOptionPane.WARNING_MESSAGE);
        }
    }
}

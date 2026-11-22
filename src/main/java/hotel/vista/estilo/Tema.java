package hotel.vista.estilo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

/**
 * Tema visual del sistema: paleta, tipografías, estilos globales de Swing
 * y fábricas de componentes decorados. Se instala una sola vez con {@link #instalar()}.
 */
public final class Tema {

    public static final Color NAVY = new Color(0x12263F);
    public static final Color NAVY_CLARO = new Color(0x24456B);
    public static final Color DORADO = new Color(0xC8A24A);
    public static final Color DORADO_OSCURO = new Color(0x9C7A28);
    public static final Color DORADO_SUAVE = new Color(0xF4EACB);
    public static final Color FONDO = new Color(0xF4F1EA);
    public static final Color FILA_ALTERNA = new Color(0xFAF8F3);
    public static final Color BORDE = new Color(0xDDD6C6);
    public static final Color TEXTO = new Color(0x1E2A38);
    public static final Color TEXTO_SUAVE = new Color(0x6B7686);
    public static final Color VERDE = new Color(0x2E7D5B);
    public static final Color ROJO = new Color(0xB23A48);
    public static final Color AMBAR = new Color(0xC98A1B);
    public static final Color AZUL = new Color(0x2F6DB5);

    public enum Variante { PRIMARIO, ACENTO, PELIGRO, SECUNDARIO }

    private static final String FAMILIA = elegirFamilia();

    private Tema() {
    }

    private static String elegirFamilia() {
        Set<String> disponibles = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String candidata : new String[]{"Segoe UI", "SF Pro Text", "Helvetica Neue", "Inter",
            "Noto Sans", "Open Sans", "DejaVu Sans"}) {
            if (disponibles.contains(candidata)) {
                return candidata;
            }
        }
        return Font.SANS_SERIF;
    }

    public static Font fuente(int estilo, float tam) {
        return new Font(FAMILIA, estilo, Math.round(tam)).deriveFont(estilo, tam);
    }

    public static Font fuenteMono(float tam) {
        return new Font(Font.MONOSPACED, Font.PLAIN, Math.round(tam));
    }

    /** Configura Look &amp; Feel, colores, fuentes e iconos de diálogo. Llamar antes de crear ventanas. */
    public static void instalar() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignorada) {
            // se usa el aspecto por defecto
        }

        UIManager.put("ButtonUI", BotonUI.class.getName());
        UIManager.put("TabbedPaneUI", TabsUI.class.getName());
        UIManager.put("ScrollBarUI", ScrollUI.class.getName());
        UIManager.put("ComboBoxUI", ComboUI.class.getName());

        Font normal = fuente(Font.PLAIN, 13);
        for (String clave : new String[]{"Label.font", "TextField.font", "PasswordField.font",
            "ComboBox.font", "Spinner.font", "List.font", "Table.font", "TextArea.font",
            "OptionPane.messageFont", "ToolTip.font", "FormattedTextField.font", "Tree.font",
            "FileChooser.listFont", "RadioButton.font", "CheckBox.font"}) {
            UIManager.put(clave, normal);
        }
        for (String clave : new String[]{"Button.font", "OptionPane.buttonFont", "TabbedPane.font",
            "TableHeader.font", "TitledBorder.font"}) {
            UIManager.put(clave, fuente(Font.BOLD, 13));
        }

        UIManager.put("Panel.background", FONDO);
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("OptionPane.messageForeground", TEXTO);
        UIManager.put("OptionPane.border", BorderFactory.createEmptyBorder(18, 20, 14, 20));
        UIManager.put("OptionPane.buttonAreaBorder", BorderFactory.createEmptyBorder(8, 12, 14, 12));
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("TextField.foreground", TEXTO);
        UIManager.put("TextField.caretForeground", NAVY);
        UIManager.put("TextField.selectionBackground", NAVY_CLARO);
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("TextArea.selectionBackground", NAVY_CLARO);
        UIManager.put("TextArea.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.selectionBackground", DORADO_SUAVE);
        UIManager.put("ComboBox.selectionForeground", NAVY);
        UIManager.put("List.selectionBackground", DORADO_SUAVE);
        UIManager.put("List.selectionForeground", NAVY);
        UIManager.put("ScrollPane.background", Color.WHITE);
        UIManager.put("ToolTip.background", NAVY);
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("ToolTip.border", BorderFactory.createEmptyBorder(6, 10, 6, 10));
        UIManager.put("Separator.foreground", BORDE);

        UIManager.put("OptionPane.informationIcon", Iconos.mensaje(javax.swing.JOptionPane.INFORMATION_MESSAGE));
        UIManager.put("OptionPane.warningIcon", Iconos.mensaje(javax.swing.JOptionPane.WARNING_MESSAGE));
        UIManager.put("OptionPane.errorIcon", Iconos.mensaje(javax.swing.JOptionPane.ERROR_MESSAGE));
        UIManager.put("OptionPane.questionIcon", Iconos.mensaje(javax.swing.JOptionPane.QUESTION_MESSAGE));
    }

    // ------------------------------------------------------------------ fábricas

    public static JButton boton(String texto, Variante variante) {
        JButton b = new JButton(texto);
        b.putClientProperty(BotonUI.PROPIEDAD, variante);
        return b;
    }

    public static JButton boton(String texto, Variante variante, Iconos icono) {
        JButton b = boton(texto, variante);
        b.setIcon(icono.comoIcono(16, variante == Variante.SECUNDARIO ? NAVY
                : variante == Variante.ACENTO ? NAVY : Color.WHITE));
        b.setIconTextGap(8);
        return b;
    }

    public static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(Font.BOLD, 12));
        l.setForeground(TEXTO_SUAVE);
        return l;
    }

    /** Campo con su etiqueta arriba (estilo formulario moderno). */
    public static JPanel campo(String etiqueta, JComponent componente) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        p.add(etiqueta(etiqueta), BorderLayout.NORTH);
        p.add(componente, BorderLayout.CENTER);
        return p;
    }

    /** Fila de campos con pesos de ancho relativos. */
    public static JPanel fila(int[] pesos, JComponent... campos) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        for (int i = 0; i < campos.length; i++) {
            c.gridx = i;
            c.weightx = pesos[i];
            c.insets = new Insets(0, i == 0 ? 0 : 14, 0, 0);
            p.add(campos[i], c);
        }
        return p;
    }

    public static JPanel apilar(int separacion, JComponent... filas) {
        JPanel p = new JPanel(new java.awt.GridLayout(0, 1, 0, separacion));
        p.setOpaque(false);
        for (JComponent f : filas) {
            p.add(f);
        }
        return p;
    }

    public static JPanel barra(int alineacion, JComponent... componentes) {
        JPanel p = new JPanel(new FlowLayout(alineacion, 10, 0));
        p.setOpaque(false);
        for (JComponent c : componentes) {
            p.add(c);
        }
        return p;
    }

    /** Panel de contenido de una ventana: transparente, con margen y separación entre bloques. */
    public static JPanel cuerpo() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(18, 22, 20, 22));
        return p;
    }

    public static JScrollPane scroll(Component contenido) {
        JScrollPane s = new JScrollPane(contenido);
        s.setBorder(BorderFactory.createLineBorder(BORDE));
        s.getViewport().setBackground(Color.WHITE);
        s.getVerticalScrollBar().setUnitIncrement(16);
        JPanel esquina = new JPanel();
        esquina.setBackground(NAVY);
        s.setCorner(JScrollPane.UPPER_RIGHT_CORNER, esquina);
        return s;
    }

    /** Etiqueta "N registro(s)" que se actualiza sola con el modelo de la tabla. */
    public static JLabel contador(TableModel modelo) {
        JLabel l = new JLabel();
        l.setFont(fuente(Font.PLAIN, 12));
        l.setForeground(TEXTO_SUAVE);
        Runnable actualizar = () -> {
            int n = modelo.getRowCount();
            l.setText(n + (n == 1 ? " registro" : " registros"));
        };
        modelo.addTableModelListener((TableModelEvent e) -> actualizar.run());
        actualizar.run();
        return l;
    }

    // ------------------------------------------------------------------ estilos

    public static void estilizarArea(JTextArea area) {
        area.setFont(fuenteMono(13));
        area.setForeground(TEXTO);
        area.setBackground(Color.WHITE);
        area.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        area.setEditable(false);
    }

    private static Border bordeCampo(boolean enfocado) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(enfocado ? DORADO : BORDE, 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10));
    }

    public static void estilizarCampo(JTextComponent campo) {
        campo.setBorder(bordeCampo(false));
        campo.setBackground(Color.WHITE);
        campo.setCaretColor(NAVY);
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                campo.setBorder(bordeCampo(true));
            }

            @Override
            public void focusLost(FocusEvent e) {
                campo.setBorder(bordeCampo(false));
            }
        });
    }

    /**
     * Recorre el árbol de componentes y aplica el estilo a campos de texto, combos
     * y spinners; vuelve transparentes los paneles simples para que se vea el fondo.
     */
    public static void aplicar(Component c) {
        if (c instanceof JSpinner spinner) {
            spinner.setBorder(BorderFactory.createLineBorder(BORDE));
            if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
                editor.getTextField().setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));
                editor.getTextField().setBackground(Color.WHITE);
            }
            return;
        }
        if (c instanceof JComboBox<?> combo) {
            combo.setBackground(Color.WHITE);
            return;
        }
        if (c instanceof JTextArea) {
            return;
        }
        if (c instanceof JTextComponent texto) {
            estilizarCampo(texto);
            return;
        }
        if (c.getClass() == JPanel.class) {
            ((JPanel) c).setOpaque(false);
        }
        if (c instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) {
                aplicar(hijo);
            }
        }
    }

    /** Prepara el fondo, el icono y el estilo de los hijos de una ventana. Llamar al final del constructor. */
    public static void preparar(JFrame ventana, Iconos icono) {
        ventana.getContentPane().setBackground(FONDO);
        if (icono != null) {
            ventana.setIconImage(icono.comoImagen(64));
        }
        for (Component hijo : ventana.getContentPane().getComponents()) {
            aplicar(hijo);
        }
    }

    public static void preparar(javax.swing.JDialog dialogo, Iconos icono) {
        dialogo.getContentPane().setBackground(FONDO);
        if (icono != null) {
            dialogo.setIconImage(icono.comoImagen(64));
        }
        for (Component hijo : dialogo.getContentPane().getComponents()) {
            aplicar(hijo);
        }
    }

    /** Estiliza un panel que se mostrará dentro de un JOptionPane. */
    public static <T extends JPanel> T dialogo(T panel) {
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 8, 4));
        for (Component hijo : panel.getComponents()) {
            if (hijo instanceof JLabel l && l.getFont().isPlain()) {
                l.setForeground(TEXTO_SUAVE);
            }
            aplicar(hijo);
        }
        return panel;
    }

    public static Dimension tam(int ancho, int alto) {
        return new Dimension(ancho, alto);
    }
}

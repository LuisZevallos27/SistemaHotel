package hotel.vista.estilo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicComboBoxUI;

/** Lista desplegable plana con borde fino y flecha tipo "chevron". */
public class ComboUI extends BasicComboBoxUI {

    public static ComponentUI createUI(JComponent c) {
        return new ComboUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        comboBox.setFont(Tema.fuente(java.awt.Font.PLAIN, 13));
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        comboBox.setRenderer(new Elemento());
    }

    @Override
    protected JButton createArrowButton() {
        JButton flecha = new JButton() {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Tema.NAVY_CLARO);
                g2.setStroke(new java.awt.BasicStroke(1.8f, java.awt.BasicStroke.CAP_ROUND,
                        java.awt.BasicStroke.JOIN_ROUND));
                double cx = getWidth() / 2.0;
                double cy = getHeight() / 2.0;
                Path2D p = new Path2D.Double();
                p.moveTo(cx - 4, cy - 2);
                p.lineTo(cx, cy + 2);
                p.lineTo(cx + 4, cy - 2);
                g2.draw(p);
                g2.dispose();
            }
        };
        flecha.setBorder(BorderFactory.createEmptyBorder());
        flecha.setFocusable(false);
        flecha.setContentAreaFilled(false);
        flecha.setPreferredSize(new Dimension(28, 28));
        flecha.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return flecha;
    }

    @Override
    public void paintCurrentValueBackground(Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {
        g.setColor(Color.WHITE);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        Dimension d = super.getMinimumSize(c);
        return new Dimension(d.width, Math.max(d.height, 34));
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension d = super.getPreferredSize(c);
        return new Dimension(d.width, Math.max(d.height, 34));
    }

    /** Renderizador con relleno interno; implementa UIResource para poder ser reemplazado. */
    private static final class Elemento extends DefaultListCellRenderer implements UIResource {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean seleccionado, boolean foco) {
            super.getListCellRendererComponent(list, value, index, seleccionado, foco);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            setForeground(seleccionado && index >= 0 ? Tema.NAVY : Tema.TEXTO);
            setBackground(seleccionado && index >= 0 ? Tema.DORADO_SUAVE : Color.WHITE);
            return this;
        }
    }
}

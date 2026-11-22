package hotel.vista.estilo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

/** Barra de desplazamiento delgada, sin flechas, con "pulgar" redondeado. */
public class ScrollUI extends BasicScrollBarUI {

    public static ComponentUI createUI(JComponent c) {
        return new ScrollUI();
    }

    private JButton vacio() {
        JButton b = new JButton();
        Dimension cero = new Dimension(0, 0);
        b.setPreferredSize(cero);
        b.setMinimumSize(cero);
        b.setMaximumSize(cero);
        return b;
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return vacio();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return vacio();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return new Dimension(12, 12);
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        g.setColor(new Color(0xF1EEE6));
        g.fillRect(r.x, r.y, r.width, r.height);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        if (r.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isThumbRollover() || isDragging ? Tema.DORADO : new Color(0xC9C2B0));
        g2.fill(new RoundRectangle2D.Double(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8));
        g2.dispose();
    }
}

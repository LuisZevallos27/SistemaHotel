package hotel.vista.estilo;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/** Pestañas planas: la activa en blanco con una línea dorada inferior. */
public class TabsUI extends BasicTabbedPaneUI {

    public static ComponentUI createUI(JComponent c) {
        return new TabsUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabInsets = new Insets(10, 24, 10, 24);
        selectedTabPadInsets = new Insets(0, 0, 0, 0);
        tabAreaInsets = new Insets(0, 0, 0, 0);
        contentBorderInsets = new Insets(1, 0, 0, 0);
        tabPane.setOpaque(false);
        tabPane.setFont(Tema.fuente(Font.BOLD, 13));
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return fontHeight + 22;
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
            int x, int y, int w, int h, boolean isSelected) {
        if (isSelected) {
            g.setColor(Color.WHITE);
            g.fillRect(x, y, w, h);
        }
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
            int x, int y, int w, int h, boolean isSelected) {
        if (isSelected) {
            g.setColor(Tema.DORADO);
            g.fillRect(x, y + h - 3, w, 3);
            g.setColor(Tema.BORDE);
            g.drawLine(x, y, x, y + h - 1);
            g.drawLine(x + w - 1, y, x + w - 1, y + h - 1);
            g.drawLine(x, y, x + w - 1, y);
        }
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
            int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(font);
        g2.setColor(isSelected ? Tema.NAVY : Tema.TEXTO_SUAVE);
        g2.drawString(title, textRect.x, textRect.y + metrics.getAscent());
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        if (selectedIndex < 0 || selectedIndex >= rects.length) {
            return;
        }
        Rectangle r = rects[selectedIndex];
        g.setColor(Tema.BORDE);
        g.drawLine(0, r.y + r.height - 1, tabPane.getWidth(), r.y + r.height - 1);
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
            int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        // sin recuadro de foco
    }
}

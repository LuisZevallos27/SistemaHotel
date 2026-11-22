package hotel.vista.estilo;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;

/** Botón plano con esquinas redondeadas y efecto hover; la variante se elige con {@link Tema#boton}. */
public class BotonUI extends BasicButtonUI {

    static final String PROPIEDAD = "hotel.variante";
    private static final Color DESHABILITADO = new Color(0xE7E3D9);
    private static final Color TEXTO_DESHABILITADO = new Color(0x9AA3AE);

    public static ComponentUI createUI(JComponent c) {
        return new BotonUI();
    }

    /** Solo se restilizan los JButton "puros" (no las flechas de spinner o scroll). */
    private static boolean propio(JComponent c) {
        return c.getClass() == JButton.class;
    }

    @Override
    protected void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        if (!propio(b)) {
            return;
        }
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
        b.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        b.setFont(Tema.fuente(java.awt.Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private Tema.Variante variante(AbstractButton b) {
        Object v = b.getClientProperty(PROPIEDAD);
        return v instanceof Tema.Variante t ? t : Tema.Variante.SECUNDARIO;
    }

    private Color fondo(AbstractButton b) {
        ButtonModel m = b.getModel();
        boolean presionado = m.isArmed() && m.isPressed();
        boolean encima = m.isRollover();
        return switch (variante(b)) {
            case PRIMARIO -> presionado ? Tema.NAVY.darker() : encima ? Tema.NAVY_CLARO : Tema.NAVY;
            case ACENTO -> presionado ? Tema.DORADO_OSCURO : encima ? new Color(0xD8B65E) : Tema.DORADO;
            case PELIGRO -> presionado ? Tema.ROJO.darker() : encima ? new Color(0xC64A58) : Tema.ROJO;
            case SECUNDARIO -> presionado ? Tema.DORADO_SUAVE.darker()
                    : encima ? Tema.DORADO_SUAVE : Color.WHITE;
        };
    }

    private Color colorTexto(AbstractButton b) {
        if (!b.isEnabled()) {
            return TEXTO_DESHABILITADO;
        }
        return switch (variante(b)) {
            case PRIMARIO, PELIGRO -> Color.WHITE;
            case ACENTO, SECUNDARIO -> Tema.NAVY;
        };
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        if (!propio(c)) {
            super.paint(g, c);
            return;
        }
        AbstractButton b = (AbstractButton) c;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = c.getWidth();
        int h = c.getHeight();
        if (!b.isEnabled()) {
            g2.setColor(DESHABILITADO);
            g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 12, 12));
        } else {
            g2.setColor(fondo(b));
            g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 12, 12));
            if (variante(b) == Tema.Variante.SECUNDARIO) {
                g2.setColor(b.getModel().isRollover() ? Tema.DORADO : Tema.BORDE);
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 2, h - 2, 12, 12));
            }
        }
        g2.dispose();
        super.paint(g, c);
    }

    @Override
    protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
        if (!propio(c)) {
            super.paintText(g, c, textRect, text);
            return;
        }
        AbstractButton b = (AbstractButton) c;
        FontMetrics fm = c.getFontMetrics(b.getFont());
        g.setFont(b.getFont());
        g.setColor(colorTexto(b));
        BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, b.getDisplayedMnemonicIndex(),
                textRect.x, textRect.y + fm.getAscent());
    }
}

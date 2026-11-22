package hotel.vista.estilo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Panel tipo "tarjeta": fondo blanco, esquinas redondeadas, sombra suave y título opcional. */
public class Tarjeta extends JPanel {

    private final Color relleno;
    private final boolean conBorde;

    public Tarjeta(String titulo, JComponent contenido) {
        this(titulo, contenido, Color.WHITE, 20);
    }

    public Tarjeta(String titulo, JComponent contenido, Color relleno, int margen) {
        super(new BorderLayout(0, 14));
        this.relleno = relleno;
        this.conBorde = Color.WHITE.equals(relleno);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(margen - 2, margen, margen, margen));
        if (titulo != null) {
            add(cabecera(titulo), BorderLayout.NORTH);
        }
        if (contenido != null) {
            add(contenido, BorderLayout.CENTER);
        }
    }

    private static JPanel cabecera(String titulo) {
        JLabel l = new JLabel(titulo);
        l.setFont(Tema.fuente(Font.BOLD, 15));
        l.setForeground(Tema.NAVY);
        JPanel barra = new JPanel();
        barra.setBackground(Tema.DORADO);
        barra.setPreferredSize(new java.awt.Dimension(30, 3));
        JPanel marco = new JPanel(new BorderLayout());
        marco.setOpaque(false);
        marco.add(barra, BorderLayout.WEST);
        JPanel p = new JPanel(new BorderLayout(0, 7));
        p.setOpaque(false);
        p.add(l, BorderLayout.CENTER);
        p.add(marco, BorderLayout.SOUTH);
        return p;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        if (conBorde) {
            g2.setColor(new Color(0, 0, 0, 10));
            g2.fill(new RoundRectangle2D.Double(1, 3, w - 2, h - 3, 18, 18));
            g2.setColor(new Color(0, 0, 0, 12));
            g2.fill(new RoundRectangle2D.Double(0, 2, w, h - 2, 18, 18));
        }
        g2.setColor(relleno);
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h - 2, 16, 16));
        if (conBorde) {
            g2.setColor(Tema.BORDE);
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 3, 16, 16));
        }
        g2.dispose();
        super.paintComponent(g);
    }
}

package hotel.vista.estilo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Banner superior de cada pantalla: degradado azul marino, insignia dorada, título y subtítulo. */
public class Encabezado extends JPanel {

    public Encabezado(String titulo, String subtitulo, Iconos icono) {
        this(titulo, subtitulo, icono, null);
    }

    public Encabezado(String titulo, String subtitulo, Iconos icono, JComponent derecha) {
        super(new BorderLayout(16, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 26, 3, 26));
        setPreferredSize(new Dimension(100, 88));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        JLabel lblSub = new JLabel(subtitulo);
        lblSub.setFont(Tema.fuente(Font.PLAIN, 13));
        lblSub.setForeground(new Color(0xC3CEDD));

        JPanel textos = new JPanel(new java.awt.GridLayout(2, 1, 0, 2));
        textos.setOpaque(false);
        textos.add(lblTitulo);
        textos.add(lblSub);
        JPanel centrado = new JPanel(new java.awt.GridBagLayout());
        centrado.setOpaque(false);
        centrado.add(textos);
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(centrado, BorderLayout.WEST);

        add(new Insignia(icono), BorderLayout.WEST);
        add(envoltorio, BorderLayout.CENTER);
        if (derecha != null) {
            JPanel lado = new JPanel(new java.awt.GridBagLayout());
            lado.setOpaque(false);
            lado.add(derecha);
            add(lado, BorderLayout.EAST);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g2.setPaint(new GradientPaint(0, 0, Tema.NAVY, w, h, Tema.NAVY_CLARO));
        g2.fillRect(0, 0, w, h);
        g2.setColor(new Color(255, 255, 255, 14));
        g2.fill(new Ellipse2D.Double(w - 190, -90, 260, 260));
        g2.setColor(new Color(200, 162, 74, 38));
        g2.fill(new Ellipse2D.Double(w - 90, 20, 150, 150));
        g2.setColor(Tema.DORADO);
        g2.fillRect(0, h - 3, w, 3);
        g2.dispose();
        super.paintComponent(g);
    }

    /** Círculo dorado con el icono de la sección. */
    private static final class Insignia extends JComponent {

        private final Iconos icono;

        Insignia(Iconos icono) {
            this.icono = icono;
            setPreferredSize(new Dimension(54, 54));
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (icono == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int y = (getHeight() - 54) / 2;
            g2.setColor(new Color(200, 162, 74, 60));
            g2.fill(new Ellipse2D.Double(0, y, 54, 54));
            g2.setColor(Tema.DORADO);
            g2.fill(new Ellipse2D.Double(4, y + 4, 46, 46));
            icono.pintar(g2, 14, y + 14, 26, Tema.NAVY);
            g2.dispose();
        }
    }
}

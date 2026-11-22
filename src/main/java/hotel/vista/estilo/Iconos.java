package hotel.vista.estilo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.Icon;

/**
 * Iconos vectoriales dibujados con Java2D (no necesitan archivos de imagen).
 * Cada icono se dibuja sobre una cuadrícula de 24x24 y se escala al tamaño pedido.
 */
public enum Iconos {
    CLIENTES, HABITACIONES, RESERVAS, ESTADIAS, OPERACIONES, COTIZADOR, REPORTES, SALIR, HOTEL;

    public void pintar(Graphics2D destino, double x, double y, double tam, Color color) {
        Graphics2D g = (Graphics2D) destino.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.translate(x, y);
        double k = tam / 24.0;
        g.scale(k, k);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (this) {
            case CLIENTES -> {
                g.draw(new Ellipse2D.Double(8, 3.5, 8, 8));
                Path2D p = new Path2D.Double();
                p.moveTo(4, 21);
                p.curveTo(4, 16, 8, 14, 12, 14);
                p.curveTo(16, 14, 20, 16, 20, 21);
                g.draw(p);
            }
            case HABITACIONES -> {
                g.draw(new Line2D.Double(3, 5, 3, 20));
                g.draw(new Line2D.Double(21, 20, 21, 14));
                g.draw(new RoundRectangle2D.Double(3, 13, 18, 4, 2, 2));
                g.draw(new RoundRectangle2D.Double(5.5, 8.5, 5.5, 4.5, 2, 2));
            }
            case RESERVAS -> {
                g.draw(new RoundRectangle2D.Double(3.5, 5, 17, 15.5, 3, 3));
                g.draw(new Line2D.Double(3.5, 10, 20.5, 10));
                g.draw(new Line2D.Double(8, 3, 8, 7));
                g.draw(new Line2D.Double(16, 3, 16, 7));
                for (double px : new double[]{7.5, 11, 14.5}) {
                    g.fill(new Rectangle2D.Double(px, 13, 2, 2));
                }
                for (double px : new double[]{7.5, 11}) {
                    g.fill(new Rectangle2D.Double(px, 16.5, 2, 2));
                }
            }
            case ESTADIAS -> {
                g.draw(new Ellipse2D.Double(3.5, 9, 7, 7));
                g.draw(new Line2D.Double(10.5, 12.5, 21, 12.5));
                g.draw(new Line2D.Double(17.5, 12.5, 17.5, 16));
                g.draw(new Line2D.Double(21, 12.5, 21, 15.5));
            }
            case OPERACIONES -> {
                g.draw(new Ellipse2D.Double(8.5, 8.5, 7, 7));
                g.draw(new Ellipse2D.Double(5.5, 5.5, 13, 13));
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4;
                    g.draw(new Line2D.Double(12 + 6.5 * Math.cos(a), 12 + 6.5 * Math.sin(a),
                            12 + 9.5 * Math.cos(a), 12 + 9.5 * Math.sin(a)));
                }
            }
            case COTIZADOR -> {
                g.draw(new RoundRectangle2D.Double(5, 3, 14, 18, 3, 3));
                g.draw(new RoundRectangle2D.Double(7.5, 5.5, 9, 4, 1, 1));
                for (double py : new double[]{13, 17}) {
                    for (double px : new double[]{9, 12, 15}) {
                        g.fill(new Ellipse2D.Double(px - 1, py - 1, 2, 2));
                    }
                }
            }
            case REPORTES -> {
                g.draw(new Line2D.Double(3.5, 20.5, 20.5, 20.5));
                g.fill(new RoundRectangle2D.Double(5.5, 12, 3.5, 8, 1, 1));
                g.fill(new RoundRectangle2D.Double(10.3, 6, 3.5, 14, 1, 1));
                g.fill(new RoundRectangle2D.Double(15, 9.5, 3.5, 10.5, 1, 1));
            }
            case SALIR -> {
                Path2D p = new Path2D.Double();
                p.moveTo(10, 4);
                p.lineTo(6, 4);
                p.quadTo(4, 4, 4, 6);
                p.lineTo(4, 18);
                p.quadTo(4, 20, 6, 20);
                p.lineTo(10, 20);
                g.draw(p);
                g.draw(new Line2D.Double(9.5, 12, 20, 12));
                Path2D flecha = new Path2D.Double();
                flecha.moveTo(16, 8);
                flecha.lineTo(20, 12);
                flecha.lineTo(16, 16);
                g.draw(flecha);
            }
            case HOTEL -> {
                g.draw(new RoundRectangle2D.Double(5, 3, 14, 18, 2, 2));
                for (double py : new double[]{6.5, 10.5}) {
                    for (double px : new double[]{8.5, 13.5}) {
                        g.fill(new Rectangle2D.Double(px, py, 2, 2));
                    }
                }
                Path2D puerta = new Path2D.Double();
                puerta.moveTo(10, 21);
                puerta.lineTo(10, 16);
                puerta.lineTo(14, 16);
                puerta.lineTo(14, 21);
                g.draw(puerta);
            }
        }
        g.dispose();
    }

    public Icon comoIcono(int tam, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                pintar((Graphics2D) g, x, y, tam, color);
            }

            @Override
            public int getIconWidth() {
                return tam;
            }

            @Override
            public int getIconHeight() {
                return tam;
            }
        };
    }

    /** Imagen para el icono de la ventana (barra de título / barra de tareas). */
    public Image comoImagen(int tam) {
        BufferedImage img = new BufferedImage(tam, tam, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Tema.NAVY);
        g.fill(new RoundRectangle2D.Double(0, 0, tam, tam, tam * 0.28, tam * 0.28));
        pintar(g, tam * 0.2, tam * 0.2, tam * 0.6, Tema.DORADO);
        g.dispose();
        return img;
    }

    /** Dibuja una estrella de cinco puntas (decoración). */
    public static void estrella(Graphics2D g, double cx, double cy, double radio) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double r = i % 2 == 0 ? radio : radio * 0.45;
            double a = -Math.PI / 2 + i * Math.PI / 5;
            double px = cx + r * Math.cos(a);
            double py = cy + r * Math.sin(a);
            if (i == 0) {
                p.moveTo(px, py);
            } else {
                p.lineTo(px, py);
            }
        }
        p.closePath();
        g.fill(p);
    }

    /** Iconos de los cuadros de diálogo (información, advertencia, error, pregunta). */
    public static Icon mensaje(int tipoJOptionPane) {
        final Color color;
        final String glifo;
        switch (tipoJOptionPane) {
            case javax.swing.JOptionPane.ERROR_MESSAGE -> {
                color = Tema.ROJO;
                glifo = "x";
            }
            case javax.swing.JOptionPane.WARNING_MESSAGE -> {
                color = Tema.AMBAR;
                glifo = "!";
            }
            case javax.swing.JOptionPane.QUESTION_MESSAGE -> {
                color = Tema.NAVY_CLARO;
                glifo = "?";
            }
            default -> {
                color = Tema.AZUL;
                glifo = "i";
            }
        }
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g0, int x, int y) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setColor(color);
                g.fill(new Ellipse2D.Double(x, y, 38, 38));
                g.setColor(Color.WHITE);
                if (glifo.equals("x")) {
                    g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.draw(new Line2D.Double(x + 13, y + 13, x + 25, y + 25));
                    g.draw(new Line2D.Double(x + 25, y + 13, x + 13, y + 25));
                } else {
                    g.setFont(Tema.fuente(Font.BOLD, 24));
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString(glifo, x + (38 - fm.stringWidth(glifo)) / 2f,
                            y + (38 - fm.getHeight()) / 2f + fm.getAscent());
                }
                g.dispose();
            }

            @Override
            public int getIconWidth() {
                return 38;
            }

            @Override
            public int getIconHeight() {
                return 38;
            }
        };
    }
}

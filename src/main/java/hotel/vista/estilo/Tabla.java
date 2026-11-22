package hotel.vista.estilo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

/**
 * Tabla decorada: encabezado azul marino, filas alternadas, selección dorada,
 * columnas de estado/prioridad como "píldoras" de color y aviso cuando no hay datos.
 */
public class Tabla extends JTable {

    public Tabla(TableModel modelo) {
        super(modelo);
        setRowHeight(34);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setFillsViewportHeight(true);
        setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
        setSelectionBackground(Tema.DORADO_SUAVE);
        setSelectionForeground(Tema.NAVY);
        setFont(Tema.fuente(java.awt.Font.PLAIN, 13));
        setDefaultRenderer(Object.class, new Celda());
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setPreferredSize(new Dimension(0, 40));
        getTableHeader().setDefaultRenderer(new Cabecera());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getRowCount() == 0) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(Tema.fuente(java.awt.Font.PLAIN, 14));
            g2.setColor(Tema.TEXTO_SUAVE);
            String msg = "Sin registros para mostrar";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, 54);
            g2.dispose();
        }
    }

    private static final class Cabecera extends DefaultTableCellRenderer {

        Cabecera() {
            setOpaque(true);
            setBackground(Tema.NAVY);
            setForeground(Color.WHITE);
            setFont(Tema.fuente(java.awt.Font.BOLD, 12));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 3, 0, Tema.DORADO),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean foco, int fila, int col) {
            setText(v == null ? "" : v.toString());
            setHorizontalAlignment(esMonto(t.getColumnName(col)) ? SwingConstants.RIGHT : SwingConstants.LEFT);
            return this;
        }
    }

    private static boolean esMonto(String columna) {
        return columna.contains("(S/)");
    }

    private static boolean esEstado(String columna) {
        String c = columna.toLowerCase();
        return c.equals("estado") || c.equals("prioridad");
    }

    private static Color colorEstado(String texto) {
        String t = texto.toUpperCase();
        if (t.contains("CANCEL") || t.contains("NO_SHOW") || t.contains("NO-SHOW")
                || t.contains("MANTENIMIENTO") || t.contains("URGENTE") || t.equals("ALTA")
                || t.contains("ABIERTO") || t.contains("FUERA")) {
            return Tema.ROJO;
        }
        if (t.contains("PENDIENTE") || t.contains("LIMPIEZA") || t.contains("MEDIA")
                || t.contains("EN_PROCESO") || t.contains("PROCESO") || t.contains("ASIGNAD")) {
            return Tema.AMBAR;
        }
        if (t.contains("CHECKIN") || t.contains("OCUPAD") || t.contains("CURSO")) {
            return Tema.AZUL;
        }
        if (t.contains("CONFIRM") || t.contains("DISPONIBLE") || t.contains("TERMINAD")
                || t.contains("CHECKOUT") || t.contains("CERRAD") || t.equals("BAJA")) {
            return Tema.VERDE;
        }
        return Tema.TEXTO_SUAVE;
    }

    private static final class Celda extends DefaultTableCellRenderer {

        private Color pildora;
        private Color fondoFila = Color.WHITE;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean foco, int fila, int col) {
            super.getTableCellRendererComponent(t, v, sel, false, fila, col);
            String columna = t.getColumnName(col);
            fondoFila = sel ? Tema.DORADO_SUAVE : fila % 2 == 0 ? Color.WHITE : Tema.FILA_ALTERNA;
            setBackground(fondoFila);
            setForeground(sel ? Tema.NAVY : Tema.TEXTO);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setHorizontalAlignment(esMonto(columna) ? SwingConstants.RIGHT : SwingConstants.LEFT);
            pildora = esEstado(columna) && v != null && !v.toString().isBlank()
                    && !"-".equals(v.toString()) ? colorEstado(v.toString()) : null;
            setToolTipText(v == null ? null : v.toString());
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (pildora == null) {
                super.paintComponent(g);
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(fondoFila);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setFont(Tema.fuente(java.awt.Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            String texto = getText();
            int ancho = Math.min(fm.stringWidth(texto) + 20, getWidth() - 16);
            int alto = 22;
            int x = 10;
            int y = (getHeight() - alto) / 2;
            g2.setColor(new Color(pildora.getRed(), pildora.getGreen(), pildora.getBlue(), 32));
            g2.fill(new RoundRectangle2D.Double(x, y, ancho, alto, alto, alto));
            g2.setColor(pildora);
            g2.drawString(texto, x + (ancho - fm.stringWidth(texto)) / 2,
                    y + (alto - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}

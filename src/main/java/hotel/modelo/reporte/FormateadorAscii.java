package hotel.modelo.reporte;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FormateadorAscii implements FormateadorReporte {

    private static final String SIN_DATOS = "(sin resultados para los filtros indicados)";

    @Override
    public String formatear(String titulo, String subtitulo, Tabla tabla) {
        int columnas = tabla.encabezados().size();
        boolean[] numerica = new boolean[columnas];
        List<List<String>> celdas = new ArrayList<>();
        for (List<Object> fila : tabla.filas()) {
            celdas.add(textos(fila));
            for (int c = 0; c < columnas; c++) {
                if (fila.get(c) instanceof Number) {
                    numerica[c] = true;
                }
            }
        }
        List<String> totales = tabla.totales() == null ? null : textos(tabla.totales());

        int[] ancho = new int[columnas];
        for (int c = 0; c < columnas; c++) {
            ancho[c] = tabla.encabezados().get(c).length();
            for (List<String> fila : celdas) {
                ancho[c] = Math.max(ancho[c], fila.get(c).length());
            }
            if (totales != null) {
                ancho[c] = Math.max(ancho[c], totales.get(c).length());
            }
        }

        int interior = suma(ancho) + 3 * (columnas - 1);
        int necesario = Math.max(titulo.length(), subtitulo.length());
        if (tabla.filas().isEmpty()) {
            necesario = Math.max(necesario, SIN_DATOS.length());
        }
        if (necesario > interior) {
            ancho[columnas - 1] += necesario - interior;
            interior = necesario;
        }

        String bloque = "+" + "-".repeat(interior + 2) + "+";
        String separador = separador(ancho);

        StringBuilder sb = new StringBuilder();
        sb.append(bloque).append('\n');
        sb.append(linea(titulo, interior)).append('\n');
        sb.append(linea(subtitulo, interior)).append('\n');
        sb.append(separador).append('\n');
        sb.append(fila(tabla.encabezados(), ancho, numerica)).append('\n');
        sb.append(separador).append('\n');
        if (celdas.isEmpty()) {
            sb.append(linea(SIN_DATOS, interior)).append('\n');
        } else {
            for (List<String> fila : celdas) {
                sb.append(fila(fila, ancho, numerica)).append('\n');
            }
        }
        sb.append(separador).append('\n');
        if (totales != null) {
            sb.append(fila(totales, ancho, numerica)).append('\n');
            sb.append(separador).append('\n');
        }
        sb.append("Filas: ").append(tabla.filas().size());
        return sb.toString();
    }

    private List<String> textos(List<Object> valores) {
        List<String> textos = new ArrayList<>();
        for (Object valor : valores) {
            textos.add(texto(valor));
        }
        return textos;
    }

    private String texto(Object valor) {
        if (valor == null) {
            return "";
        }
        if (valor instanceof Number numero) {
            boolean entero = numero instanceof Integer || numero instanceof Long
                    || numero instanceof Short;
            return entero ? String.valueOf(numero)
                    : String.format(Locale.US, "%,.2f", numero.doubleValue());
        }
        return valor.toString();
    }

    private String fila(List<String> valores, int[] ancho, boolean[] numerica) {
        StringBuilder sb = new StringBuilder("|");
        for (int c = 0; c < ancho.length; c++) {
            String valor = valores.get(c);
            String relleno = " ".repeat(ancho[c] - valor.length());
            sb.append(' ').append(numerica[c] ? relleno + valor : valor + relleno).append(" |");
        }
        return sb.toString();
    }

    private String linea(String texto, int interior) {
        return "| " + texto + " ".repeat(interior - texto.length()) + " |";
    }

    private String separador(int[] ancho) {
        StringBuilder sb = new StringBuilder("+");
        for (int columna : ancho) {
            sb.append("-".repeat(columna + 2)).append('+');
        }
        return sb.toString();
    }

    private int suma(int[] valores) {
        int total = 0;
        for (int valor : valores) {
            total += valor;
        }
        return total;
    }
}

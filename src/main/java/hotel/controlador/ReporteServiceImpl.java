package hotel.controlador;

import hotel.dao.ReporteConsulta;
import hotel.excepcion.NegocioException;
import hotel.modelo.reporte.DefinicionReporte;
import hotel.modelo.reporte.FormateadorReporte;
import hotel.modelo.reporte.ParametroReporte;
import hotel.modelo.reporte.Tabla;
import hotel.modelo.reporte.TipoParametro;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ReporteServiceImpl implements ReporteService {

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ReporteConsulta consulta;
    private final FormateadorReporte formateador;
    private final List<DefinicionReporte> catalogo;

    public ReporteServiceImpl(ReporteConsulta consulta, FormateadorReporte formateador,
            List<DefinicionReporte> catalogo) {
        this.consulta = consulta;
        this.formateador = formateador;
        this.catalogo = List.copyOf(catalogo);
    }

    @Override
    public List<DefinicionReporte> reportes() {
        return catalogo;
    }

    @Override
    public String generar(DefinicionReporte definicion, List<String> valores, String generadoPor)
            throws NegocioException {
        List<Object> argumentos = convertir(definicion, valores);
        Tabla datos = consulta.ejecutar(definicion, argumentos);
        Tabla completa = new Tabla(datos.encabezados(), datos.filas(), totalesDe(definicion, datos));

        String subtitulo = "Generado: " + LocalDateTime.now().format(FORMATO_FECHA_HORA)
                + " | Por: " + generadoPor + filtros(definicion, argumentos);
        return formateador.formatear("REPORTE: " + definicion.nombre().toUpperCase(), subtitulo, completa);
    }

    private List<Object> convertir(DefinicionReporte definicion, List<String> textos)
            throws NegocioException {
        List<Object> valores = new ArrayList<>();
        List<LocalDate> fechas = new ArrayList<>();
        for (int i = 0; i < definicion.parametros().size(); i++) {
            ParametroReporte parametro = definicion.parametros().get(i);
            String texto = i < textos.size() && textos.get(i) != null ? textos.get(i).trim() : "";
            if (texto.isEmpty()) {
                if (parametro.obligatorio()) {
                    throw new NegocioException("Indique: " + parametro.etiqueta() + ".");
                }
                valores.add(null);
                continue;
            }
            if (parametro.tipo() == TipoParametro.FECHA) {
                try {
                    LocalDate fecha = LocalDate.parse(texto);
                    fechas.add(fecha);
                    valores.add(fecha);
                } catch (DateTimeParseException e) {
                    throw new NegocioException(parametro.etiqueta() + ": use el formato AAAA-MM-DD.");
                }
            } else {
                try {
                    int numero = Integer.parseInt(texto);
                    if (numero <= 0) {
                        throw new NegocioException(parametro.etiqueta() + " debe ser mayor que cero.");
                    }
                    valores.add(numero);
                } catch (NumberFormatException e) {
                    throw new NegocioException(parametro.etiqueta() + ": ingrese un número entero.");
                }
            }
        }
        if (fechas.size() == 2 && fechas.get(0).isAfter(fechas.get(1))) {
            throw new NegocioException("La fecha \"Desde\" no puede ser posterior a \"Hasta\".");
        }
        return valores;
    }

    private List<Object> totalesDe(DefinicionReporte definicion, Tabla datos) {
        if (definicion.columnasTotal().isEmpty() || datos.filas().isEmpty()) {
            return null;
        }
        List<Object> totales = new ArrayList<>();
        for (int c = 0; c < definicion.encabezados().size(); c++) {
            if (!definicion.columnasTotal().contains(c)) {
                totales.add(c == 0 ? "TOTAL" : null);
                continue;
            }
            boolean decimal = false;
            double suma = 0;
            for (List<Object> fila : datos.filas()) {
                if (fila.get(c) instanceof Number numero) {
                    suma += numero.doubleValue();
                    if (!(numero instanceof Integer || numero instanceof Long || numero instanceof Short)) {
                        decimal = true;
                    }
                }
            }
            if (decimal) {
                totales.add(Double.valueOf(suma));
            } else {
                totales.add(Long.valueOf(Math.round(suma)));
            }
        }
        return totales;
    }

    private String filtros(DefinicionReporte definicion, List<Object> argumentos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < argumentos.size(); i++) {
            if (argumentos.get(i) != null) {
                sb.append(" | ").append(definicion.parametros().get(i).etiqueta())
                        .append(": ").append(argumentos.get(i));
            }
        }
        return sb.toString();
    }
}

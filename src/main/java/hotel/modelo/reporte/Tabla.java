package hotel.modelo.reporte;

import java.util.List;

public record Tabla(List<String> encabezados, List<List<Object>> filas, List<Object> totales) {

}

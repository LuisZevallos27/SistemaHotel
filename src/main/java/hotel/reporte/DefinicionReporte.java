package hotel.reporte;

import java.util.List;

public record DefinicionReporte(String nombre, String descripcion, String procedimiento,
        List<String> encabezados, List<Integer> columnasTotal,
        List<ParametroReporte> parametros) {

    @Override
    public String toString() {
        return nombre;
    }
}

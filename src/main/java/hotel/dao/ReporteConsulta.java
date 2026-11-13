package hotel.dao;

import hotel.reporte.DefinicionReporte;
import hotel.reporte.Tabla;
import java.util.List;

public interface ReporteConsulta {

    Tabla ejecutar(DefinicionReporte definicion, List<Object> valores);
}

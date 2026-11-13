package hotel.dao;

import hotel.modelo.reporte.DefinicionReporte;
import hotel.modelo.reporte.Tabla;
import java.util.List;

public interface ReporteConsulta {

    Tabla ejecutar(DefinicionReporte definicion, List<Object> valores);
}

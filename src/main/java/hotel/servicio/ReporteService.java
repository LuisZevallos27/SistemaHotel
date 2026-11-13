package hotel.servicio;

import hotel.excepcion.NegocioException;
import hotel.reporte.DefinicionReporte;
import java.util.List;

public interface ReporteService {

    List<DefinicionReporte> reportes();

    String generar(DefinicionReporte definicion, List<String> valores, String generadoPor)
            throws NegocioException;
}

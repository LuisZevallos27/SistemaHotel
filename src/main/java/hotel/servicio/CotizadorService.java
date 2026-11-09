package hotel.servicio;

import hotel.excepcion.NegocioException;
import hotel.tarifa.Cotizacion;
import java.time.LocalDate;
import java.util.List;

public interface CotizadorService {

    List<String> tiposHabitacion();

    Cotizacion cotizar(String tipoHabitacion, LocalDate entrada, LocalDate salida,
            String documentoCliente, String codigoPromocion) throws NegocioException;
}

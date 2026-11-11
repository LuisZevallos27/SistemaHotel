package hotel.servicio;

import hotel.excepcion.NegocioException;
import hotel.tarifa.Cotizacion;
import java.time.LocalDate;
import java.util.List;

public interface CotizadorService {

    List<String> tiposHabitacion();

    List<String> canales();

    Cotizacion cotizar(String tipoHabitacion, LocalDate entrada, LocalDate salida,
            String documentoCliente, String codigoPromocion, String canal)
            throws NegocioException;
}

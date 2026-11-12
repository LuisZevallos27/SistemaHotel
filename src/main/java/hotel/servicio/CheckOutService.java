package hotel.servicio;

import hotel.estadia.Estadia;
import hotel.excepcion.NegocioException;
import hotel.facturacion.MetodoPago;
import hotel.facturacion.ResultadoCheckOut;
import hotel.facturacion.TipoComprobante;

public interface CheckOutService {

    ResultadoCheckOut finalizar(Estadia estadia, TipoComprobante tipo, MetodoPago metodo,
            String referencia, Integer idUsuario) throws NegocioException;

    void registrarEncuesta(Estadia estadia, int puntaje, String comentario) throws NegocioException;
}

package hotel.controlador;

import hotel.modelo.estadia.Estadia;
import hotel.excepcion.NegocioException;
import hotel.modelo.facturacion.MetodoPago;
import hotel.modelo.facturacion.ResultadoCheckOut;
import hotel.modelo.facturacion.TipoComprobante;

public interface CheckOutService {

    ResultadoCheckOut finalizar(Estadia estadia, TipoComprobante tipo, MetodoPago metodo,
            String referencia, Integer idUsuario) throws NegocioException;

    void registrarEncuesta(Estadia estadia, int puntaje, String comentario) throws NegocioException;
}

package hotel.dao;

import hotel.facturacion.MetodoPago;

public interface PagoEscritura {

    void registrarPagoFactura(int idFactura, MetodoPago metodo, double monto,
            String referencia, Integer idUsuario);

    void registrarPagoReserva(int idReserva, MetodoPago metodo, double monto,
            String referencia, Integer idUsuario);
}

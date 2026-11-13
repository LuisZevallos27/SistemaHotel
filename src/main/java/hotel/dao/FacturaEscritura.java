package hotel.dao;

import hotel.modelo.facturacion.NuevaFactura;

public interface FacturaEscritura {

    int emitir(NuevaFactura factura);

    void anular(int idFactura);
}

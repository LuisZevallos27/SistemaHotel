package hotel.facturacion;

import java.util.List;

public record ResultadoCheckOut(Factura factura, List<LineaFactura> lineas,
        double montoPagado, double anticipos) {

}

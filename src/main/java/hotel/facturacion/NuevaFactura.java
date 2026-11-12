package hotel.facturacion;

import java.util.List;

public record NuevaFactura(int idEstadia, int idCliente, Integer idEmpresa, TipoComprobante tipo,
        double subtotal, double igv, double total, Integer idUsuario,
        List<LineaFactura> lineas) {

}

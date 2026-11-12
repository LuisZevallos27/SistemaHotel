package hotel.facturacion;

import java.time.LocalDateTime;

public record Factura(int idFactura, TipoComprobante tipo, String serie, int numero,
        double subtotal, double igv, double total, LocalDateTime fechaEmision,
        String estado, String cliente, String documento, String empresa, String ruc) {

    public String numeroCompleto() {
        return String.format("%s-%06d", serie, numero);
    }
}

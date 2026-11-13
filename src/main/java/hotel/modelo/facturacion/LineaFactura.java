package hotel.modelo.facturacion;

public record LineaFactura(String concepto, int cantidad, double precioUnitario) {

    public double subtotal() {
        return Math.round(cantidad * precioUnitario * 100.0) / 100.0;
    }
}

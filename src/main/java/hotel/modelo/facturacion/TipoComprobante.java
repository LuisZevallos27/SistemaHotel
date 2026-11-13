package hotel.modelo.facturacion;

public enum TipoComprobante {
    BOLETA("B001"), FACTURA("F001");

    private final String serie;

    TipoComprobante(String serie) {
        this.serie = serie;
    }

    public String serie() {
        return serie;
    }
}

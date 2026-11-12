package hotel.facturacion;

public enum MetodoPago {
    EFECTIVO, TARJETA, TRANSFERENCIA, PASARELA, CREDITO;

    public boolean admiteAnticipo() {
        return this != CREDITO;
    }
}

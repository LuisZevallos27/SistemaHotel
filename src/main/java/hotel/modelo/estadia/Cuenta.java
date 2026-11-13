package hotel.modelo.estadia;

public record Cuenta(ElementoCuenta detalle, double anticipos) {

    public double total() {
        return detalle.total();
    }

    public double saldo() {
        return Math.round((detalle.total() - anticipos) * 100.0) / 100.0;
    }
}

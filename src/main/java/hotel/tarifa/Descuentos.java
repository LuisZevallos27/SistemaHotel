package hotel.tarifa;

public record Descuentos(String empresa, double descuentoEmpresa, String nivel, double descuentoNivel) {

    public static final Descuentos NINGUNO = new Descuentos(null, 0, null, 0);
}

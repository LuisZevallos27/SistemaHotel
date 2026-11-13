package hotel.modelo.canal;

public record Canal(int id, String nombre, boolean requiereAnticipo, double porcentajeAnticipo,
        int diasCancelacionGratis, boolean confirmacionAutomatica,
        double descuentoPorcentaje, double comisionPorcentaje) {

}

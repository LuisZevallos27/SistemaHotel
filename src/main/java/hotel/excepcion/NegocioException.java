package hotel.excepcion;

public class NegocioException extends Exception {

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}

package hotel.excepcion;

import java.sql.SQLException;

public class AccesoDatosException extends RuntimeException {

    private static final int PRIMER_ERROR_NEGOCIO = 50001;
    private static final int ULTIMO_ERROR_NEGOCIO = 50030;

    public AccesoDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public static AccesoDatosException desde(SQLException e, String mensajeGenerico) {
        int codigo = e.getErrorCode();
        if (codigo >= PRIMER_ERROR_NEGOCIO && codigo <= ULTIMO_ERROR_NEGOCIO) {
            return new AccesoDatosException(e.getMessage(), null);
        }
        return new AccesoDatosException(mensajeGenerico, e);
    }
}

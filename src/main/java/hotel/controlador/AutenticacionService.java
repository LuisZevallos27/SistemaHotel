package hotel.controlador;

import hotel.excepcion.NegocioException;
import hotel.modelo.seguridad.Usuario;

public interface AutenticacionService {

    Usuario iniciarSesion(String username, char[] clave) throws NegocioException;
}

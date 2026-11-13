package hotel.servicio;

import hotel.excepcion.NegocioException;
import hotel.seguridad.Usuario;

public interface AutenticacionService {

    Usuario iniciarSesion(String username, char[] clave) throws NegocioException;
}

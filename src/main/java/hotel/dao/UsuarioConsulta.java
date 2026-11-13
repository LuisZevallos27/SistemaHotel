package hotel.dao;

import hotel.modelo.seguridad.Credencial;
import java.util.Optional;

public interface UsuarioConsulta {

    Optional<Credencial> buscarCredencial(String username);
}

package hotel.servicio;

import hotel.dao.UsuarioConsulta;
import hotel.excepcion.NegocioException;
import hotel.seguridad.Credencial;
import hotel.seguridad.Usuario;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

public class AutenticacionServiceImpl implements AutenticacionService {

    private final UsuarioConsulta usuarios;

    public AutenticacionServiceImpl(UsuarioConsulta usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public Usuario iniciarSesion(String username, char[] clave) throws NegocioException {
        if (username == null || username.trim().isEmpty() || clave == null || clave.length == 0) {
            throw new NegocioException("Ingrese usuario y contraseña.");
        }
        Optional<Credencial> credencial = usuarios.buscarCredencial(username.trim());
        String hashIngresado = hash(clave);
        if (credencial.isEmpty() || !coincide(hashIngresado, credencial.get().hash())) {
            throw new NegocioException("Usuario o contraseña incorrectos.");
        }
        if (!credencial.get().usuario().activo()) {
            throw new NegocioException("El usuario está inactivo. Contacte al administrador.");
        }
        return credencial.get().usuario();
    }

    private String hash(char[] clave) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = new String(clave).getBytes(StandardCharsets.UTF_8);
            return HexFormat.of().withUpperCase().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("El algoritmo SHA-256 no está disponible.", e);
        }
    }

    private boolean coincide(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}

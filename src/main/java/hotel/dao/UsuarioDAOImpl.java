package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.seguridad.Credencial;
import hotel.seguridad.Usuario;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UsuarioDAOImpl implements UsuarioConsulta {

    private final ProveedorConexion proveedor;

    public UsuarioDAOImpl(ProveedorConexion proveedor) {
        this.proveedor = proveedor;
    }

    @Override
    public Optional<Credencial> buscarCredencial(String username) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Usuario_ObtenerPorUsername(?)}")) {
            cs.setString(1, username);
            try (ResultSet rs = cs.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Usuario usuario = new Usuario(rs.getInt("idUsuario"), rs.getString("username"),
                        rs.getString("nombreCompleto"), rs.getString("rol"), rs.getBoolean("activo"));
                return Optional.of(new Credencial(usuario, rs.getString("passwordHash")));
            }
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo consultar el usuario.");
        }
    }
}

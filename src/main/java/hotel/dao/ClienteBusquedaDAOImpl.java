package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.modelo.Cliente;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ClienteBusquedaDAOImpl implements ClienteBusqueda {

    private final ProveedorConexion proveedor;

    public ClienteBusquedaDAOImpl(ProveedorConexion proveedor) {
        this.proveedor = proveedor;
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String documento) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Cliente_BuscarPorDocumento(?)}")) {
            cs.setString(1, documento);
            try (ResultSet rs = cs.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Cliente c = new Cliente();
                c.setIdCliente(rs.getInt("idCliente"));
                c.setTipoDocumento(rs.getString("tipoDocumento"));
                c.setDocumento(rs.getString("documento"));
                c.setNombres(rs.getString("nombres"));
                c.setApellidos(rs.getString("apellidos"));
                c.setTelefono(rs.getString("telefono"));
                c.setCorreo(rs.getString("correo"));
                c.setNacionalidad(rs.getString("nacionalidad"));
                int empresa = rs.getInt("idEmpresa");
                c.setIdEmpresa(rs.wasNull() ? null : empresa);
                return Optional.of(c);
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo buscar el cliente.", e);
        }
    }
}

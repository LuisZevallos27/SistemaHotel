package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.modelo.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAOImpl implements ClienteConsulta, ClienteEscritura {

    private final ProveedorConexion proveedor;

    public ClienteDAOImpl(ProveedorConexion proveedor) {
        this.proveedor = proveedor;
    }

    @Override
    public List<Cliente> listar() {
        String sql = "SELECT idCliente, tipoDocumento, documento, nombres, apellidos, "
                + "telefono, correo, nacionalidad FROM Cliente "
                + "WHERE activo = 1 ORDER BY apellidos, nombres";
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cliente c = new Cliente();
                c.setIdCliente(rs.getInt("idCliente"));
                c.setTipoDocumento(rs.getString("tipoDocumento"));
                c.setDocumento(rs.getString("documento"));
                c.setNombres(rs.getString("nombres"));
                c.setApellidos(rs.getString("apellidos"));
                c.setTelefono(rs.getString("telefono"));
                c.setCorreo(rs.getString("correo"));
                c.setNacionalidad(rs.getString("nacionalidad"));
                lista.add(c);
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener la lista de clientes.", e);
        }
        return lista;
    }

    @Override
    public boolean existeDocumento(String documento, int idExcluido) {
        String sql = "SELECT COUNT(*) FROM Cliente WHERE documento = ? AND idCliente <> ?";
        try (Connection cn = proveedor.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, documento);
            ps.setInt(2, idExcluido);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo verificar el documento.", e);
        }
    }

    @Override
    public void insertar(Cliente c) {
        String sql = "INSERT INTO Cliente (tipoDocumento, documento, nombres, apellidos, "
                + "telefono, correo, nacionalidad) VALUES (?,?,?,?,?,?,?)";
        try (Connection cn = proveedor.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getTipoDocumento());
            ps.setString(2, c.getDocumento());
            ps.setString(3, c.getNombres());
            ps.setString(4, c.getApellidos());
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getCorreo());
            ps.setString(7, c.getNacionalidad());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el cliente.", e);
        }
    }

    @Override
    public void actualizar(Cliente c) {
        String sql = "UPDATE Cliente SET tipoDocumento=?, documento=?, nombres=?, apellidos=?, "
                + "telefono=?, correo=?, nacionalidad=? WHERE idCliente=?";
        try (Connection cn = proveedor.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getTipoDocumento());
            ps.setString(2, c.getDocumento());
            ps.setString(3, c.getNombres());
            ps.setString(4, c.getApellidos());
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getCorreo());
            ps.setString(7, c.getNacionalidad());
            ps.setInt(8, c.getIdCliente());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el cliente.", e);
        }
    }

    @Override
    public void desactivar(int idCliente) {
        try (Connection cn = proveedor.getConexion(); PreparedStatement ps = cn.prepareStatement(
                "UPDATE Cliente SET activo = 0 WHERE idCliente = ?")) {
            ps.setInt(1, idCliente);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo eliminar el cliente.", e);
        }
    }
}

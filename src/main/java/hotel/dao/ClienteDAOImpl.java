package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.modelo.Cliente;
import java.sql.CallableStatement;
import java.sql.Connection;
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
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Cliente_Listar}"); ResultSet rs = cs.executeQuery()) {
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
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Cliente_ExisteDocumento(?, ?)}")) {
            cs.setString(1, documento);
            cs.setInt(2, idExcluido);
            try (ResultSet rs = cs.executeQuery()) {
                rs.next();
                return rs.getInt("existe") == 1;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo verificar el documento.", e);
        }
    }

    @Override
    public void insertar(Cliente c) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Cliente_Insertar(?, ?, ?, ?, ?, ?, ?)}")) {
            cs.setString(1, c.getTipoDocumento());
            cs.setString(2, c.getDocumento());
            cs.setString(3, c.getNombres());
            cs.setString(4, c.getApellidos());
            cs.setString(5, c.getTelefono());
            cs.setString(6, c.getCorreo());
            cs.setString(7, c.getNacionalidad());
            cs.execute();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el cliente.", e);
        }
    }

    @Override
    public void actualizar(Cliente c) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Cliente_Actualizar(?, ?, ?, ?, ?, ?, ?, ?)}")) {
            cs.setInt(1, c.getIdCliente());
            cs.setString(2, c.getTipoDocumento());
            cs.setString(3, c.getDocumento());
            cs.setString(4, c.getNombres());
            cs.setString(5, c.getApellidos());
            cs.setString(6, c.getTelefono());
            cs.setString(7, c.getCorreo());
            cs.setString(8, c.getNacionalidad());
            cs.execute();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el cliente.", e);
        }
    }

    @Override
    public void desactivar(int idCliente) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Cliente_Desactivar(?)}")) {
            cs.setInt(1, idCliente);
            cs.execute();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo eliminar el cliente.", e);
        }
    }
}

package hotel.dao;

import hotel.config.ConexionBD;
import hotel.modelo.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAOImpl implements ClienteDAO {

    @Override
    public List<Cliente> listar() throws SQLException {
        String sql = "SELECT idCliente, tipoDocumento, documento, nombres, apellidos, "
                + "telefono, correo, nacionalidad FROM Cliente "
                + "WHERE activo = 1 ORDER BY apellidos, nombres";
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getInstancia().getConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
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
        }
        return lista;
    }

    @Override
    public void insertar(Cliente c) throws SQLException {
        // El cliente nuevo entra con nivel Bronce
        String sql = "INSERT INTO Cliente (tipoDocumento, documento, nombres, apellidos, "
                + "telefono, correo, nacionalidad, idNivel) VALUES (?,?,?,?,?,?,?, "
                + "(SELECT idNivel FROM NivelFidelidad WHERE nombre = N'Bronce'))";
        try (Connection cn = ConexionBD.getInstancia().getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getTipoDocumento());
            ps.setString(2, c.getDocumento());
            ps.setString(3, c.getNombres());
            ps.setString(4, c.getApellidos());
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getCorreo());
            ps.setString(7, c.getNacionalidad());
            ps.executeUpdate();
        }
    }

    @Override
    public void actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE Cliente SET tipoDocumento=?, documento=?, nombres=?, apellidos=?, "
                + "telefono=?, correo=?, nacionalidad=? WHERE idCliente=?";
        try (Connection cn = ConexionBD.getInstancia().getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getTipoDocumento());
            ps.setString(2, c.getDocumento());
            ps.setString(3, c.getNombres());
            ps.setString(4, c.getApellidos());
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getCorreo());
            ps.setString(7, c.getNacionalidad());
            ps.setInt(8, c.getIdCliente());
            ps.executeUpdate();
        }
    }

    // Baja lógica: el cliente puede tener reservas e historial, por eso no se borra la fila.
    @Override
    public void desactivar(int idCliente) throws SQLException {
        try (Connection cn = ConexionBD.getInstancia().getConexion(); PreparedStatement ps = cn.prepareStatement(
                "UPDATE Cliente SET activo = 0 WHERE idCliente = ?")) {
            ps.setInt(1, idCliente);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean existeDocumento(String documento, int idExcluido) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Cliente WHERE documento = ? AND idCliente <> ?";
        try (Connection cn = ConexionBD.getInstancia().getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, documento);
            ps.setInt(2, idExcluido);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}

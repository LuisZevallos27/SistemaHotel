package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.habitacion.TipoHabitacion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TipoHabitacionDAOImpl implements TipoHabitacionConsulta {

    private final ProveedorConexion proveedor;

    public TipoHabitacionDAOImpl(ProveedorConexion proveedor) {
        this.proveedor = proveedor;
    }

    @Override
    public List<TipoHabitacion> listar() {
        String sql = "SELECT idTipo, nombre, capacidad, tarifaBase FROM TipoHabitacion ORDER BY tarifaBase";
        List<TipoHabitacion> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new TipoHabitacion(rs.getInt("idTipo"), rs.getString("nombre"),
                        rs.getInt("capacidad"), rs.getBigDecimal("tarifaBase").doubleValue()));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener los tipos de habitación.", e);
        }
        return lista;
    }
}

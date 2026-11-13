package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.modelo.habitacion.TipoHabitacion;
import java.sql.CallableStatement;
import java.sql.Connection;
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
        List<TipoHabitacion> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_TipoHabitacion_Listar}"); ResultSet rs = cs.executeQuery()) {
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

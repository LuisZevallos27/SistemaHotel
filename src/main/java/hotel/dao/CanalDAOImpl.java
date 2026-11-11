package hotel.dao;

import hotel.canal.Canal;
import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CanalDAOImpl implements CanalConsulta {

    private final ProveedorConexion proveedor;

    public CanalDAOImpl(ProveedorConexion proveedor) {
        this.proveedor = proveedor;
    }

    @Override
    public List<Canal> listar() {
        List<Canal> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Canal_Listar}"); ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                lista.add(new Canal(rs.getInt("idCanal"), rs.getString("nombre"),
                        rs.getBoolean("requiereAnticipo"),
                        rs.getBigDecimal("porcentajeAnticipo").doubleValue(),
                        rs.getInt("diasCancelacionGratis"),
                        rs.getBoolean("confirmacionAutomatica"),
                        rs.getBigDecimal("descuentoPorcentaje").doubleValue(),
                        rs.getBigDecimal("comisionPorcentaje").doubleValue()));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener los canales de reserva.", e);
        }
        return lista;
    }
}

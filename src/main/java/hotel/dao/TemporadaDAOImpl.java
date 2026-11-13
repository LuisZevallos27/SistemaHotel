package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.modelo.tarifa.Temporada;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TemporadaDAOImpl implements TemporadaConsulta {

    private final ProveedorConexion proveedor;

    public TemporadaDAOImpl(ProveedorConexion proveedor) {
        this.proveedor = proveedor;
    }

    @Override
    public List<Temporada> listar() {
        List<Temporada> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Temporada_Listar}"); ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                lista.add(new Temporada(rs.getInt("idTemporada"), rs.getString("nombre"),
                        rs.getDate("fechaInicio").toLocalDate(), rs.getDate("fechaFin").toLocalDate(),
                        rs.getBigDecimal("factor").doubleValue()));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener las temporadas.", e);
        }
        return lista;
    }
}

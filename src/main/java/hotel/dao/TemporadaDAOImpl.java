package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.tarifa.Temporada;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
        String sql = "SELECT idTemporada, nombre, fechaInicio, fechaFin, factor "
                + "FROM Temporada ORDER BY fechaInicio";
        List<Temporada> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
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

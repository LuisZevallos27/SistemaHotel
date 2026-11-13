package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.modelo.tarifa.Promocion;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class PromocionDAOImpl implements PromocionConsulta {

    private final ProveedorConexion proveedor;

    public PromocionDAOImpl(ProveedorConexion proveedor) {
        this.proveedor = proveedor;
    }

    @Override
    public Optional<Promocion> buscarVigentePorCodigo(String codigo, LocalDate fecha) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Promocion_BuscarVigente(?, ?)}")) {
            cs.setString(1, codigo);
            cs.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = cs.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                int tipo = rs.getInt("idTipo");
                Integer idTipo = rs.wasNull() ? null : tipo;
                return Optional.of(new Promocion(rs.getInt("idPromocion"), rs.getString("codigo"),
                        rs.getString("nombre"), rs.getString("tipoDescuento"),
                        rs.getBigDecimal("valor").doubleValue(),
                        rs.getDate("fechaInicio").toLocalDate(), rs.getDate("fechaFin").toLocalDate(),
                        rs.getInt("minNoches"), idTipo));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar la promoción.", e);
        }
    }
}

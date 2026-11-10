package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.tarifa.Descuentos;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class DescuentoClienteDAOImpl implements DescuentoClienteConsulta {

    private final ProveedorConexion proveedor;

    public DescuentoClienteDAOImpl(ProveedorConexion proveedor) {
        this.proveedor = proveedor;
    }

    @Override
    public Optional<Descuentos> buscarPorDocumento(String documento) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Cliente_ObtenerDescuentos(?)}")) {
            cs.setString(1, documento);
            try (ResultSet rs = cs.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Descuentos(rs.getString("razonSocial"),
                        rs.getBigDecimal("descEmpresa").doubleValue(),
                        rs.getString("nivel"),
                        rs.getBigDecimal("descNivel").doubleValue()));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar los descuentos del cliente.", e);
        }
    }
}

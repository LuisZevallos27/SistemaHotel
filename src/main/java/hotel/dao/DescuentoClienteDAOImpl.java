package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.tarifa.Descuentos;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
        String sql = "SELECT e.razonSocial, ISNULL(e.descuentoPorcentaje, 0) AS descEmpresa, "
                + "n.nombre AS nivel, ISNULL(n.descuentoPorcentaje, 0) AS descNivel "
                + "FROM Cliente c "
                + "LEFT JOIN Empresa e ON e.idEmpresa = c.idEmpresa "
                + "LEFT JOIN NivelFidelidad n ON n.idNivel = c.idNivel "
                + "WHERE c.documento = ? AND c.activo = 1";
        try (Connection cn = proveedor.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
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

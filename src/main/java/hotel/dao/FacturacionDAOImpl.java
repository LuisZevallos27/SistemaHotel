package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.facturacion.Factura;
import hotel.facturacion.LineaFactura;
import hotel.facturacion.MetodoPago;
import hotel.facturacion.NuevaFactura;
import hotel.facturacion.TipoComprobante;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class FacturacionDAOImpl implements FacturaConsulta, FacturaEscritura, PagoEscritura {

    private final ProveedorConexion proveedor;

    public FacturacionDAOImpl(ProveedorConexion proveedor) {
        this.proveedor = proveedor;
    }

    @Override
    public Optional<Factura> vigentePorEstadia(int idEstadia) {
        Integer idVigente = null;
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Factura_ListarPorEstadia(?)}")) {
            cs.setInt(1, idEstadia);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    if (!"ANULADA".equals(rs.getString("estado"))) {
                        idVigente = rs.getInt("idFactura");
                    }
                }
            }
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo consultar las facturas de la estadía.");
        }
        return idVigente == null ? Optional.empty() : buscarPorId(idVigente);
    }

    @Override
    public Optional<Factura> buscarPorId(int idFactura) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Factura_ObtenerPorId(?)}")) {
            cs.setInt(1, idFactura);
            try (ResultSet rs = cs.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Factura(rs.getInt("idFactura"),
                        TipoComprobante.valueOf(rs.getString("tipoComprobante")),
                        rs.getString("serie"), rs.getInt("numero"),
                        rs.getBigDecimal("subtotal").doubleValue(),
                        rs.getBigDecimal("igv").doubleValue(),
                        rs.getBigDecimal("total").doubleValue(),
                        rs.getTimestamp("fechaEmision").toLocalDateTime(),
                        rs.getString("estado"), rs.getString("cliente"), rs.getString("documento"),
                        rs.getString("empresa"), rs.getString("ruc")));
            }
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo consultar la factura.");
        }
    }

    @Override
    public List<LineaFactura> lineas(int idFactura) {
        List<LineaFactura> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_DetalleFactura_ListarPorFactura(?)}")) {
            cs.setInt(1, idFactura);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new LineaFactura(rs.getString("concepto"), rs.getInt("cantidad"),
                            rs.getBigDecimal("precioUnitario").doubleValue()));
                }
            }
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo consultar el detalle de la factura.");
        }
        return lista;
    }

    @Override
    public int emitir(NuevaFactura f) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall(
                "{call usp_Factura_Emitir(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")) {
            cs.setInt(1, f.idEstadia());
            cs.setInt(2, f.idCliente());
            asignarEntero(cs, 3, f.idEmpresa());
            cs.setString(4, f.tipo().name());
            cs.setString(5, f.tipo().serie());
            cs.setBigDecimal(6, BigDecimal.valueOf(f.subtotal()));
            cs.setBigDecimal(7, BigDecimal.valueOf(f.igv()));
            cs.setBigDecimal(8, BigDecimal.valueOf(f.total()));
            asignarEntero(cs, 9, f.idUsuario());
            SQLXML detalle = cn.createSQLXML();
            detalle.setString(construirXml(f.lineas()));
            cs.setSQLXML(10, detalle);
            try (ResultSet rs = cs.executeQuery()) {
                rs.next();
                return rs.getInt("idFactura");
            }
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo emitir la factura.");
        }
    }

    @Override
    public void anular(int idFactura) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Factura_Anular(?)}")) {
            cs.setInt(1, idFactura);
            cs.execute();
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo anular la factura.");
        }
    }

    @Override
    public void registrarPagoFactura(int idFactura, MetodoPago metodo, double monto,
            String referencia, Integer idUsuario) {
        registrarPago(null, idFactura, metodo, monto, referencia, idUsuario);
    }

    @Override
    public void registrarPagoReserva(int idReserva, MetodoPago metodo, double monto,
            String referencia, Integer idUsuario) {
        registrarPago(idReserva, null, metodo, monto, referencia, idUsuario);
    }

    private void registrarPago(Integer idReserva, Integer idFactura, MetodoPago metodo,
            double monto, String referencia, Integer idUsuario) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Pago_Registrar(?, ?, ?, ?, ?, ?)}")) {
            asignarEntero(cs, 1, idReserva);
            asignarEntero(cs, 2, idFactura);
            cs.setString(3, metodo.name());
            cs.setBigDecimal(4, BigDecimal.valueOf(monto));
            cs.setString(5, referencia);
            asignarEntero(cs, 6, idUsuario);
            cs.execute();
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo registrar el pago.");
        }
    }

    private String construirXml(List<LineaFactura> lineas) {
        StringBuilder sb = new StringBuilder("<detalle>");
        for (LineaFactura l : lineas) {
            sb.append("<item concepto=\"").append(escapar(l.concepto()))
                    .append("\" cantidad=\"").append(l.cantidad())
                    .append("\" precioUnitario=\"")
                    .append(String.format(Locale.US, "%.2f", l.precioUnitario()))
                    .append("\"/>");
        }
        return sb.append("</detalle>").toString();
    }

    private String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    private void asignarEntero(CallableStatement cs, int indice, Integer valor) throws SQLException {
        if (valor == null) {
            cs.setNull(indice, Types.INTEGER);
        } else {
            cs.setInt(indice, valor);
        }
    }
}

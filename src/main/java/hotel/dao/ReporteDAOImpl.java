package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.reporte.DefinicionReporte;
import hotel.reporte.Tabla;
import hotel.reporte.TipoParametro;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAOImpl implements ReporteConsulta {

    private final ProveedorConexion proveedor;

    public ReporteDAOImpl(ProveedorConexion proveedor) {
        this.proveedor = proveedor;
    }

    @Override
    public Tabla ejecutar(DefinicionReporte definicion, List<Object> valores) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall(llamada(definicion, valores.size()))) {
            for (int i = 0; i < valores.size(); i++) {
                asignar(cs, i + 1, definicion.parametros().get(i).tipo(), valores.get(i));
            }
            try (ResultSet rs = cs.executeQuery()) {
                int columnas = rs.getMetaData().getColumnCount();
                List<List<Object>> filas = new ArrayList<>();
                while (rs.next()) {
                    List<Object> fila = new ArrayList<>();
                    for (int c = 1; c <= columnas; c++) {
                        fila.add(rs.getObject(c));
                    }
                    filas.add(fila);
                }
                return new Tabla(definicion.encabezados(), filas, null);
            }
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo generar el reporte.");
        }
    }

    private String llamada(DefinicionReporte definicion, int parametros) {
        StringBuilder sb = new StringBuilder("{call ").append(definicion.procedimiento());
        if (parametros > 0) {
            sb.append('(').append("?, ".repeat(parametros - 1)).append("?)");
        }
        return sb.append('}').toString();
    }

    private void asignar(CallableStatement cs, int indice, TipoParametro tipo, Object valor)
            throws SQLException {
        if (valor == null) {
            cs.setNull(indice, tipo == TipoParametro.FECHA ? Types.DATE : Types.INTEGER);
        } else if (tipo == TipoParametro.FECHA) {
            cs.setDate(indice, Date.valueOf((LocalDate) valor));
        } else {
            cs.setInt(indice, (Integer) valor);
        }
    }
}

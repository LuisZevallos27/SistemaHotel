package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.modelo.estadia.Consumo;
import hotel.modelo.estadia.Estadia;
import hotel.modelo.estadia.Servicio;
import hotel.excepcion.AccesoDatosException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EstadiaDAOImpl implements EstadiaConsulta, EstadiaEscritura {

    private final ProveedorConexion proveedor;

    public EstadiaDAOImpl(ProveedorConexion proveedor) {
        this.proveedor = proveedor;
    }

    @Override
    public List<Estadia> listarEnCurso() {
        List<Estadia> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Estadia_ListarEnCurso}"); ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                lista.add(new Estadia(rs.getInt("idEstadia"), rs.getInt("idReserva"),
                        rs.getString("codigo"), rs.getInt("idHabitacion"), rs.getString("habitacion"),
                        fecha(rs.getTimestamp("fechaCheckIn")), fecha(rs.getTimestamp("fechaCheckOut")),
                        rs.getString("estado"), rs.getInt("idCliente"), rs.getString("cliente"),
                        rs.getString("documento"), rs.getDate("fechaEntrada").toLocalDate(),
                        rs.getDate("fechaSalida").toLocalDate(),
                        rs.getBigDecimal("tarifaNoche").doubleValue(),
                        rs.getBigDecimal("total").doubleValue()));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener las estadías en curso.", e);
        }
        return lista;
    }

    @Override
    public List<Servicio> listarServicios() {
        List<Servicio> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Servicio_Listar}"); ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                lista.add(new Servicio(rs.getInt("idServicio"), rs.getString("nombre"),
                        rs.getString("area"), rs.getBigDecimal("precio").doubleValue()));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener los servicios adicionales.", e);
        }
        return lista;
    }

    @Override
    public List<Consumo> consumosDe(int idEstadia) {
        List<Consumo> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Consumo_ListarPorEstadia(?)}")) {
            cs.setInt(1, idEstadia);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Consumo(rs.getInt("idConsumo"), rs.getString("servicio"),
                            rs.getString("area"), rs.getInt("cantidad"),
                            rs.getBigDecimal("precioUnitario").doubleValue(),
                            fecha(rs.getTimestamp("fecha"))));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener los consumos de la estadía.", e);
        }
        return lista;
    }

    @Override
    public double anticiposPagados(int idEstadia) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Estadia_ResumenCuenta(?)}")) {
            cs.setInt(1, idEstadia);
            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("anticipos").doubleValue() : 0;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener los anticipos de la estadía.", e);
        }
    }

    @Override
    public int registrarCheckIn(int idReserva, int idHabitacion, Integer idUsuario) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Estadia_CheckIn(?, ?, ?)}")) {
            cs.setInt(1, idReserva);
            cs.setInt(2, idHabitacion);
            asignarEntero(cs, 3, idUsuario);
            try (ResultSet rs = cs.executeQuery()) {
                rs.next();
                return rs.getInt("idEstadia");
            }
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo registrar el check-in.");
        }
    }

    @Override
    public void registrarConsumo(int idEstadia, int idServicio, int cantidad, Integer idUsuario) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Consumo_Registrar(?, ?, ?, ?)}")) {
            cs.setInt(1, idEstadia);
            cs.setInt(2, idServicio);
            cs.setInt(3, cantidad);
            asignarEntero(cs, 4, idUsuario);
            cs.execute();
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo registrar el consumo.");
        }
    }

    @Override
    public void registrarCheckOut(int idEstadia, Integer idUsuario) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Estadia_CheckOut(?, ?)}")) {
            cs.setInt(1, idEstadia);
            asignarEntero(cs, 2, idUsuario);
            cs.execute();
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo registrar el check-out.");
        }
    }

    @Override
    public void registrarEncuesta(int idEstadia, int puntaje, String comentario) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Encuesta_Registrar(?, ?, ?)}")) {
            cs.setInt(1, idEstadia);
            cs.setInt(2, puntaje);
            cs.setString(3, comentario);
            cs.execute();
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo registrar la encuesta (¿ya fue registrada?).");
        }
    }

    private void asignarEntero(CallableStatement cs, int indice, Integer valor) throws SQLException {
        if (valor == null) {
            cs.setNull(indice, Types.INTEGER);
        } else {
            cs.setInt(indice, valor);
        }
    }

    private LocalDateTime fecha(Timestamp marca) {
        return marca == null ? null : marca.toLocalDateTime();
    }
}

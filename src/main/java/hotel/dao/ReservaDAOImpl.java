package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.modelo.reserva.EstadosReserva;
import hotel.modelo.reserva.RegistroHistorial;
import hotel.modelo.reserva.Reserva;
import hotel.modelo.reserva.ReservaBuilder;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservaDAOImpl implements ReservaConsulta, ReservaEscritura {

    private static final int ERROR_SOBREVENTA = 50001;

    private final ProveedorConexion proveedor;

    public ReservaDAOImpl(ProveedorConexion proveedor) {
        this.proveedor = proveedor;
    }

    @Override
    public List<Reserva> listar(String estado) {
        List<Reserva> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Reserva_Listar(?)}")) {
            cs.setString(1, estado);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener la lista de reservas.", e);
        }
        return lista;
    }

    @Override
    public Optional<Reserva> buscarPorId(int idReserva) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Reserva_ObtenerPorId(?)}")) {
            cs.setInt(1, idReserva);
            try (ResultSet rs = cs.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapear(rs));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar la reserva.", e);
        }
    }

    @Override
    public List<RegistroHistorial> historial(int idReserva) {
        List<RegistroHistorial> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Reserva_Historial(?)}")) {
            cs.setInt(1, idReserva);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    Timestamp fecha = rs.getTimestamp("fecha");
                    lista.add(new RegistroHistorial(fecha.toLocalDateTime(),
                            rs.getString("estadoAnterior"), rs.getString("estadoNuevo"),
                            rs.getString("username"), rs.getString("detalle")));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener el historial de la reserva.", e);
        }
        return lista;
    }

    @Override
    public Reserva insertar(Reserva r, Integer idUsuario) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall(
                "{call usp_Reserva_Crear(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")) {
            cs.setInt(1, r.getIdCliente());
            cs.setInt(2, r.getIdTipo());
            asignarEntero(cs, 3, r.getIdHabitacion());
            cs.setInt(4, r.getIdCanal());
            asignarEntero(cs, 5, r.getIdPromocion());
            asignarEntero(cs, 6, r.getIdPaquete());
            asignarEntero(cs, 7, idUsuario);
            cs.setDate(8, Date.valueOf(r.getFechaEntrada()));
            cs.setDate(9, Date.valueOf(r.getFechaSalida()));
            cs.setInt(10, r.getAdultos());
            cs.setInt(11, r.getNinos());
            cs.setBigDecimal(12, BigDecimal.valueOf(r.getTarifaNoche()));
            cs.setBigDecimal(13, BigDecimal.valueOf(r.getDescuento()));
            cs.setBigDecimal(14, BigDecimal.valueOf(r.getTotal()));
            cs.setBigDecimal(15, BigDecimal.valueOf(r.getAnticipo()));
            cs.setString(16, r.getEstado().nombre());
            cs.setString(17, r.getObservaciones());
            try (ResultSet rs = cs.executeQuery()) {
                rs.next();
                return ReservaBuilder.desde(r)
                        .id(rs.getInt("idReserva"))
                        .codigo(rs.getString("codigo"))
                        .build();
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ERROR_SOBREVENTA) {
                throw new AccesoDatosException(
                        "La habitación ya fue reservada en esas fechas. Busque la disponibilidad nuevamente.", e);
            }
            throw new AccesoDatosException("No se pudo guardar la reserva.", e);
        }
    }

    @Override
    public void cambiarEstado(int idReserva, String estado, Integer idUsuario, String detalle) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Reserva_CambiarEstado(?, ?, ?, ?)}")) {
            cs.setInt(1, idReserva);
            cs.setString(2, estado);
            asignarEntero(cs, 3, idUsuario);
            cs.setString(4, detalle);
            cs.execute();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo cambiar el estado de la reserva.", e);
        }
    }

    private void asignarEntero(CallableStatement cs, int indice, Integer valor) throws SQLException {
        if (valor == null) {
            cs.setNull(indice, Types.INTEGER);
        } else {
            cs.setInt(indice, valor);
        }
    }

    private Reserva mapear(ResultSet rs) throws SQLException {
        int hab = rs.getInt("idHabitacion");
        Integer idHabitacion = rs.wasNull() ? null : hab;
        int promo = rs.getInt("idPromocion");
        Integer idPromocion = rs.wasNull() ? null : promo;
        int paq = rs.getInt("idPaquete");
        Integer idPaquete = rs.wasNull() ? null : paq;
        return new ReservaBuilder()
                .id(rs.getInt("idReserva"))
                .codigo(rs.getString("codigo"))
                .cliente(rs.getInt("idCliente"), rs.getString("cliente"), rs.getString("documento"))
                .tipo(rs.getInt("idTipo"), rs.getString("tipo"))
                .habitacion(idHabitacion, rs.getString("habitacion"))
                .canal(rs.getInt("idCanal"), rs.getString("canal"))
                .promocion(idPromocion)
                .paquete(idPaquete)
                .fechas(rs.getDate("fechaEntrada").toLocalDate(), rs.getDate("fechaSalida").toLocalDate())
                .huespedes(rs.getInt("numAdultos"), rs.getInt("numNinos"))
                .precio(rs.getBigDecimal("tarifaNoche").doubleValue(),
                        rs.getBigDecimal("descuento").doubleValue(),
                        rs.getBigDecimal("total").doubleValue())
                .anticipo(rs.getBigDecimal("anticipo").doubleValue())
                .estado(EstadosReserva.desde(rs.getString("estado")))
                .observaciones(rs.getString("observaciones"))
                .build();
    }
}

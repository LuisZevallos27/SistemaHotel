package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.habitacion.CreadorHabitaciones;
import hotel.habitacion.DatosHabitacion;
import hotel.habitacion.EstadoHabitacion;
import hotel.habitacion.Habitacion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HabitacionDAOImpl implements HabitacionConsulta, HabitacionEscritura {

    private final ProveedorConexion proveedor;
    private final CreadorHabitaciones creador;

    public HabitacionDAOImpl(ProveedorConexion proveedor, CreadorHabitaciones creador) {
        this.proveedor = proveedor;
        this.creador = creador;
    }

    @Override
    public List<Habitacion> listar() {
        String sqlAmenidades = "SELECT ta.idTipo, a.nombre FROM TipoHabitacionAmenidad ta "
                + "JOIN Amenidad a ON a.idAmenidad = ta.idAmenidad ORDER BY a.nombre";
        String sqlHabitaciones = "SELECT h.idHabitacion, h.numero, h.piso, h.estado, "
                + "t.idTipo, t.nombre AS tipo, t.capacidad, t.tarifaBase "
                + "FROM Habitacion h JOIN TipoHabitacion t ON t.idTipo = h.idTipo "
                + "WHERE h.activa = 1 ORDER BY h.numero";
        List<Habitacion> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion()) {
            Map<Integer, List<String>> amenidades = new HashMap<>();
            try (PreparedStatement ps = cn.prepareStatement(sqlAmenidades); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    amenidades.computeIfAbsent(rs.getInt("idTipo"), k -> new ArrayList<>())
                            .add(rs.getString("nombre"));
                }
            }
            try (PreparedStatement ps = cn.prepareStatement(sqlHabitaciones); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo");
                    DatosHabitacion datos = new DatosHabitacion(
                            rs.getInt("idHabitacion"),
                            rs.getString("numero"),
                            rs.getInt("piso"),
                            EstadoHabitacion.desde(rs.getString("estado")),
                            rs.getInt("capacidad"),
                            rs.getBigDecimal("tarifaBase").doubleValue(),
                            amenidades.getOrDefault(rs.getInt("idTipo"), new ArrayList<>()));
                    lista.add(creador.crear(tipo, datos).orElseThrow(() -> new AccesoDatosException(
                            "No hay fábrica registrada para el tipo de habitación: " + tipo, null)));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener la lista de habitaciones.", e);
        }
        return lista;
    }

    @Override
    public void actualizarEstado(int idHabitacion, EstadoHabitacion estado) {
        String sql = "UPDATE Habitacion SET estado = ? WHERE idHabitacion = ?";
        try (Connection cn = proveedor.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, idHabitacion);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo cambiar el estado de la habitación.", e);
        }
    }
}

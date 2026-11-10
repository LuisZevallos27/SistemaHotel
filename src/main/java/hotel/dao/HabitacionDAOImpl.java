package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.habitacion.CreadorHabitaciones;
import hotel.habitacion.DatosHabitacion;
import hotel.habitacion.EstadoHabitacion;
import hotel.habitacion.Habitacion;
import java.sql.CallableStatement;
import java.sql.Connection;
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
        List<Habitacion> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion()) {
            Map<Integer, List<String>> amenidades = new HashMap<>();
            try (CallableStatement cs = cn.prepareCall("{call usp_Amenidad_ListarPorTipo}"); ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    amenidades.computeIfAbsent(rs.getInt("idTipo"), k -> new ArrayList<>())
                            .add(rs.getString("nombre"));
                }
            }
            try (CallableStatement cs = cn.prepareCall("{call usp_Habitacion_Listar}"); ResultSet rs = cs.executeQuery()) {
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
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Habitacion_ActualizarEstado(?, ?)}")) {
            cs.setInt(1, idHabitacion);
            cs.setString(2, estado.name());
            cs.execute();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo cambiar el estado de la habitación.", e);
        }
    }
}

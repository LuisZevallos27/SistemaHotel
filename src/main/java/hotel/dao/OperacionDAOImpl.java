package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.operacion.Empleado;
import hotel.operacion.EstadoTarea;
import hotel.operacion.Mantenimiento;
import hotel.operacion.Prioridad;
import hotel.operacion.TareaLimpieza;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OperacionDAOImpl implements LimpiezaDAO, MantenimientoDAO {

    private final ProveedorConexion proveedor;

    public OperacionDAOImpl(ProveedorConexion proveedor) {
        this.proveedor = proveedor;
    }

    @Override
    public List<TareaLimpieza> listarTareas(String estado) {
        List<TareaLimpieza> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_TareaLimpieza_Listar(?)}")) {
            cs.setString(1, estado);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new TareaLimpieza(rs.getInt("idTarea"), rs.getInt("idHabitacion"),
                            rs.getString("habitacion"), rs.getString("tipo"),
                            EstadoTarea.valueOf(rs.getString("estado")), rs.getString("asignado"),
                            fecha(rs.getTimestamp("fechaCreacion"))));
                }
            }
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo obtener las tareas de limpieza.");
        }
        return lista;
    }

    @Override
    public List<Empleado> usuarios() {
        List<Empleado> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Usuario_Listar}"); ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                lista.add(new Empleado(rs.getInt("idUsuario"), rs.getString("nombreCompleto"),
                        rs.getString("rol"), rs.getBoolean("activo")));
            }
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo obtener el personal.");
        }
        return lista;
    }

    @Override
    public void asignar(int idTarea, int idUsuario) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_TareaLimpieza_Asignar(?, ?)}")) {
            cs.setInt(1, idTarea);
            cs.setInt(2, idUsuario);
            cs.execute();
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo asignar la tarea.");
        }
    }

    @Override
    public void cambiarEstado(int idTarea, EstadoTarea estado) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_TareaLimpieza_CambiarEstado(?, ?)}")) {
            cs.setInt(1, idTarea);
            cs.setString(2, estado.name());
            cs.execute();
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo cambiar el estado de la tarea.");
        }
    }

    @Override
    public List<Mantenimiento> listarMantenimientos(String estado) {
        List<Mantenimiento> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Mantenimiento_Listar(?)}")) {
            cs.setString(1, estado);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Mantenimiento(rs.getInt("idMantenimiento"), rs.getInt("idHabitacion"),
                            rs.getString("habitacion"), rs.getString("descripcion"),
                            Prioridad.valueOf(rs.getString("prioridad")), rs.getString("estado"),
                            fecha(rs.getTimestamp("fechaReporte")), fecha(rs.getTimestamp("fechaCierre")),
                            rs.getString("reporta")));
                }
            }
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo obtener los reportes de mantenimiento.");
        }
        return lista;
    }

    @Override
    public void reportar(int idHabitacion, String descripcion, Prioridad prioridad, Integer idUsuario) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Mantenimiento_Reportar(?, ?, ?, ?)}")) {
            cs.setInt(1, idHabitacion);
            cs.setString(2, descripcion);
            cs.setString(3, prioridad.name());
            if (idUsuario == null) {
                cs.setNull(4, Types.INTEGER);
            } else {
                cs.setInt(4, idUsuario);
            }
            cs.execute();
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo registrar el reporte de mantenimiento.");
        }
    }

    @Override
    public void cerrar(int idMantenimiento) {
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Mantenimiento_Cerrar(?)}")) {
            cs.setInt(1, idMantenimiento);
            cs.execute();
        } catch (SQLException e) {
            throw AccesoDatosException.desde(e, "No se pudo cerrar el reporte de mantenimiento.");
        }
    }

    private LocalDateTime fecha(Timestamp marca) {
        return marca == null ? null : marca.toLocalDateTime();
    }
}

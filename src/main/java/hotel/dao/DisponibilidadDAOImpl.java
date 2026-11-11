package hotel.dao;

import hotel.config.ProveedorConexion;
import hotel.excepcion.AccesoDatosException;
import hotel.habitacion.HabitacionLibre;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DisponibilidadDAOImpl implements DisponibilidadConsulta {

    private final ProveedorConexion proveedor;

    public DisponibilidadDAOImpl(ProveedorConexion proveedor) {
        this.proveedor = proveedor;
    }

    @Override
    public List<HabitacionLibre> buscar(int idTipo, LocalDate entrada, LocalDate salida) {
        List<HabitacionLibre> lista = new ArrayList<>();
        try (Connection cn = proveedor.getConexion(); CallableStatement cs = cn.prepareCall("{call usp_Habitacion_BuscarDisponibles(?, ?, ?)}")) {
            cs.setInt(1, idTipo);
            cs.setDate(2, Date.valueOf(entrada));
            cs.setDate(3, Date.valueOf(salida));
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new HabitacionLibre(rs.getInt("idHabitacion"),
                            rs.getString("numero"), rs.getInt("piso")));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar la disponibilidad.", e);
        }
        return lista;
    }
}

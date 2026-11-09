package hotel;

import hotel.config.ConexionBD;
import hotel.config.ProveedorConexion;
import hotel.dao.ClienteDAOImpl;
import hotel.dao.HabitacionDAOImpl;
import hotel.documento.RegistroFabricasDocumento;
import hotel.habitacion.RegistroFabricasHabitacion;
import hotel.servicio.ClienteService;
import hotel.servicio.ClienteServiceImpl;
import hotel.servicio.HabitacionService;
import hotel.servicio.HabitacionServiceImpl;
import hotel.vista.FrmPrincipal;
import javax.swing.SwingUtilities;

public class SistemaHotel {

    public static void main(String[] args) {
        ProveedorConexion conexion = ConexionBD.getInstancia();

        ClienteDAOImpl clienteDao = new ClienteDAOImpl(conexion);
        ClienteService clientes = new ClienteServiceImpl(
                clienteDao, clienteDao, new RegistroFabricasDocumento());

        HabitacionDAOImpl habitacionDao = new HabitacionDAOImpl(
                conexion, new RegistroFabricasHabitacion());
        HabitacionService habitaciones = new HabitacionServiceImpl(habitacionDao, habitacionDao);

        SwingUtilities.invokeLater(() -> new FrmPrincipal(clientes, habitaciones).setVisible(true));
    }
}

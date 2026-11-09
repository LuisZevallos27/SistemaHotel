package hotel;

import hotel.config.ConexionBD;
import hotel.config.ProveedorConexion;
import hotel.dao.ClienteDAOImpl;
import hotel.dao.DescuentoClienteDAOImpl;
import hotel.dao.HabitacionDAOImpl;
import hotel.dao.PromocionDAOImpl;
import hotel.dao.TemporadaDAOImpl;
import hotel.dao.TipoHabitacionDAOImpl;
import hotel.documento.RegistroFabricasDocumento;
import hotel.habitacion.RegistroFabricasHabitacion;
import hotel.servicio.ClienteService;
import hotel.servicio.ClienteServiceImpl;
import hotel.servicio.CotizadorService;
import hotel.servicio.CotizadorServiceImpl;
import hotel.servicio.HabitacionService;
import hotel.servicio.HabitacionServiceImpl;
import hotel.tarifa.FabricaTarifa;
import hotel.tarifa.FabricaTarifaConvenio;
import hotel.tarifa.FabricaTarifaFidelidad;
import hotel.tarifa.FabricaTarifaPromocion;
import hotel.tarifa.FabricaTarifaTemporada;
import hotel.vista.FrmPrincipal;
import java.util.List;
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

        List<FabricaTarifa> reglasTarifa = List.of(
                new FabricaTarifaTemporada(),
                new FabricaTarifaConvenio(),
                new FabricaTarifaFidelidad(),
                new FabricaTarifaPromocion());
        CotizadorService cotizador = new CotizadorServiceImpl(
                new TipoHabitacionDAOImpl(conexion),
                new TemporadaDAOImpl(conexion),
                new PromocionDAOImpl(conexion),
                new DescuentoClienteDAOImpl(conexion),
                reglasTarifa);

        SwingUtilities.invokeLater(
                () -> new FrmPrincipal(clientes, habitaciones, cotizador).setVisible(true));
    }
}

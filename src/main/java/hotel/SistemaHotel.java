package hotel;

import hotel.config.ConexionBD;
import hotel.config.ProveedorConexion;
import hotel.dao.CanalDAOImpl;
import hotel.dao.ClienteBusquedaDAOImpl;
import hotel.dao.ClienteDAOImpl;
import hotel.dao.DescuentoClienteDAOImpl;
import hotel.dao.DisponibilidadDAOImpl;
import hotel.dao.HabitacionDAOImpl;
import hotel.dao.PromocionDAOImpl;
import hotel.dao.ReservaDAOImpl;
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
import hotel.servicio.ReservaService;
import hotel.servicio.ReservaServiceImpl;
import hotel.tarifa.FabricaTarifa;
import hotel.tarifa.FabricaTarifaCanal;
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

        TipoHabitacionDAOImpl tipoDao = new TipoHabitacionDAOImpl(conexion);
        CanalDAOImpl canalDao = new CanalDAOImpl(conexion);

        List<FabricaTarifa> reglasTarifa = List.of(
                new FabricaTarifaTemporada(),
                new FabricaTarifaConvenio(),
                new FabricaTarifaFidelidad(),
                new FabricaTarifaCanal(),
                new FabricaTarifaPromocion());
        CotizadorService cotizador = new CotizadorServiceImpl(
                tipoDao,
                new TemporadaDAOImpl(conexion),
                new PromocionDAOImpl(conexion),
                new DescuentoClienteDAOImpl(conexion),
                canalDao,
                reglasTarifa);

        ReservaDAOImpl reservaDao = new ReservaDAOImpl(conexion);
        ReservaService reservas = new ReservaServiceImpl(
                reservaDao, reservaDao, canalDao,
                new DisponibilidadDAOImpl(conexion),
                new ClienteBusquedaDAOImpl(conexion),
                tipoDao, cotizador);

        SwingUtilities.invokeLater(() -> new FrmPrincipal(
                clientes, habitaciones, reservas, cotizador).setVisible(true));
    }
}

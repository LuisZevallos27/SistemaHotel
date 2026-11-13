package hotel;

import hotel.config.ConexionBD;
import hotel.config.ProveedorConexion;
import hotel.dao.CanalDAOImpl;
import hotel.dao.ClienteBusquedaDAOImpl;
import hotel.dao.ClienteDAOImpl;
import hotel.dao.DescuentoClienteDAOImpl;
import hotel.dao.DisponibilidadDAOImpl;
import hotel.dao.EstadiaDAOImpl;
import hotel.dao.FacturacionDAOImpl;
import hotel.dao.HabitacionDAOImpl;
import hotel.dao.OperacionDAOImpl;
import hotel.dao.PromocionDAOImpl;
import hotel.dao.ReservaDAOImpl;
import hotel.dao.TemporadaDAOImpl;
import hotel.dao.TipoHabitacionDAOImpl;
import hotel.dao.UsuarioDAOImpl;
import hotel.documento.RegistroFabricasDocumento;
import hotel.habitacion.RegistroFabricasHabitacion;
import hotel.seguridad.Modulo;
import hotel.seguridad.PoliticaAcceso;
import hotel.seguridad.Usuario;
import hotel.servicio.AutenticacionService;
import hotel.servicio.AutenticacionServiceImpl;
import hotel.servicio.CheckOutFacade;
import hotel.servicio.CheckOutService;
import hotel.servicio.ClienteService;
import hotel.servicio.ClienteServiceImpl;
import hotel.servicio.CotizadorService;
import hotel.servicio.CotizadorServiceImpl;
import hotel.servicio.EstadiaService;
import hotel.servicio.EstadiaServiceImpl;
import hotel.servicio.HabitacionService;
import hotel.servicio.HabitacionServiceImpl;
import hotel.servicio.LimpiezaService;
import hotel.servicio.LimpiezaServiceImpl;
import hotel.servicio.MantenimientoService;
import hotel.servicio.MantenimientoServiceImpl;
import hotel.servicio.ReservaService;
import hotel.servicio.ReservaServiceImpl;
import hotel.servicio.Servicios;
import hotel.tarifa.FabricaTarifa;
import hotel.tarifa.FabricaTarifaCanal;
import hotel.tarifa.FabricaTarifaConvenio;
import hotel.tarifa.FabricaTarifaFidelidad;
import hotel.tarifa.FabricaTarifaPromocion;
import hotel.tarifa.FabricaTarifaTemporada;
import hotel.vista.FrmLogin;
import hotel.vista.FrmPrincipal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;

public class SistemaHotel {

    private final AutenticacionService autenticacion;
    private final PoliticaAcceso politica;
    private final Servicios servicios;

    private SistemaHotel() {
        ProveedorConexion conexion = ConexionBD.getInstancia();

        ClienteDAOImpl clienteDao = new ClienteDAOImpl(conexion);
        ClienteService clientes = new ClienteServiceImpl(
                clienteDao, clienteDao, new RegistroFabricasDocumento());
        ClienteBusquedaDAOImpl clienteBusqueda = new ClienteBusquedaDAOImpl(conexion);

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

        DisponibilidadDAOImpl disponibilidadDao = new DisponibilidadDAOImpl(conexion);
        FacturacionDAOImpl facturacionDao = new FacturacionDAOImpl(conexion);

        ReservaDAOImpl reservaDao = new ReservaDAOImpl(conexion);
        ReservaService reservas = new ReservaServiceImpl(
                reservaDao, reservaDao, canalDao, disponibilidadDao,
                clienteBusqueda, tipoDao, cotizador, facturacionDao);

        EstadiaDAOImpl estadiaDao = new EstadiaDAOImpl(conexion);
        EstadiaService estadias = new EstadiaServiceImpl(
                reservaDao, disponibilidadDao, habitacionDao, estadiaDao, estadiaDao);

        CheckOutService checkOut = new CheckOutFacade(
                estadias, clienteBusqueda, facturacionDao, facturacionDao, facturacionDao, estadiaDao);

        OperacionDAOImpl operacionDao = new OperacionDAOImpl(conexion);
        LimpiezaService limpieza = new LimpiezaServiceImpl(operacionDao);
        MantenimientoService mantenimiento = new MantenimientoServiceImpl(operacionDao);

        this.servicios = new Servicios(clientes, habitaciones, reservas, estadias,
                checkOut, cotizador, limpieza, mantenimiento);
        this.autenticacion = new AutenticacionServiceImpl(new UsuarioDAOImpl(conexion));
        this.politica = definirPolitica();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SistemaHotel().mostrarLogin());
    }

    private void mostrarLogin() {
        new FrmLogin(autenticacion, this::abrirPrincipal).setVisible(true);
    }

    private void abrirPrincipal(Usuario usuario) {
        new FrmPrincipal(usuario, politica, servicios, this::mostrarLogin).setVisible(true);
    }

    private PoliticaAcceso definirPolitica() {
        Map<String, Set<Modulo>> permisos = new HashMap<>();
        permisos.put("Administrador", EnumSet.allOf(Modulo.class));
        permisos.put("Recepción", EnumSet.of(Modulo.CLIENTES, Modulo.HABITACIONES, Modulo.RESERVAS,
                Modulo.ESTADIAS, Modulo.COTIZADOR));
        permisos.put("Caja", EnumSet.of(Modulo.RESERVAS, Modulo.ESTADIAS, Modulo.COTIZADOR));
        permisos.put("Housekeeping", EnumSet.of(Modulo.HABITACIONES, Modulo.OPERACIONES));
        permisos.put("Gerencia", EnumSet.of(Modulo.HABITACIONES, Modulo.RESERVAS, Modulo.COTIZADOR,
                Modulo.REPORTES));
        return new PoliticaAcceso(permisos);
    }
}

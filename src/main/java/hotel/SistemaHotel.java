package hotel;

import hotel.modelo.reporte.ExportadorTxt;
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
import hotel.dao.ReporteDAOImpl;
import hotel.dao.ReservaDAOImpl;
import hotel.dao.TemporadaDAOImpl;
import hotel.dao.TipoHabitacionDAOImpl;
import hotel.dao.UsuarioDAOImpl;
import hotel.modelo.documento.RegistroFabricasDocumento;
import hotel.modelo.habitacion.RegistroFabricasHabitacion;
import hotel.modelo.reporte.CatalogoReportes;
import hotel.modelo.reporte.FormateadorAscii;
import hotel.modelo.seguridad.Modulo;
import hotel.modelo.seguridad.PoliticaAcceso;
import hotel.modelo.seguridad.Usuario;
import hotel.controlador.AutenticacionService;
import hotel.controlador.AutenticacionServiceImpl;
import hotel.controlador.CheckOutFacade;
import hotel.controlador.CheckOutService;
import hotel.controlador.ClienteService;
import hotel.controlador.ClienteServiceImpl;
import hotel.controlador.CotizadorService;
import hotel.controlador.CotizadorServiceImpl;
import hotel.controlador.EstadiaService;
import hotel.controlador.EstadiaServiceImpl;
import hotel.controlador.HabitacionService;
import hotel.controlador.HabitacionServiceImpl;
import hotel.controlador.LimpiezaService;
import hotel.controlador.LimpiezaServiceImpl;
import hotel.controlador.MantenimientoService;
import hotel.controlador.MantenimientoServiceImpl;
import hotel.controlador.ReporteService;
import hotel.controlador.ReporteServiceImpl;
import hotel.controlador.ReservaService;
import hotel.controlador.ReservaServiceImpl;
import hotel.controlador.Servicios;
import hotel.modelo.tarifa.FabricaTarifa;
import hotel.modelo.tarifa.FabricaTarifaCanal;
import hotel.modelo.tarifa.FabricaTarifaConvenio;
import hotel.modelo.tarifa.FabricaTarifaFidelidad;
import hotel.modelo.tarifa.FabricaTarifaPromocion;
import hotel.modelo.tarifa.FabricaTarifaTemporada;
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

        ReporteService reportes = new ReporteServiceImpl(
                new ReporteDAOImpl(conexion), new FormateadorAscii(), new ExportadorTxt(),
                CatalogoReportes.todos());

        this.servicios = new Servicios(clientes, habitaciones, reservas, estadias,
                checkOut, cotizador, limpieza, mantenimiento, reportes);
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

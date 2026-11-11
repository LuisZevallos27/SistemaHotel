package hotel.servicio;

import hotel.canal.Canal;
import hotel.dao.CanalConsulta;
import hotel.dao.ClienteBusqueda;
import hotel.dao.DisponibilidadConsulta;
import hotel.dao.ReservaConsulta;
import hotel.dao.ReservaEscritura;
import hotel.dao.TipoHabitacionConsulta;
import hotel.excepcion.NegocioException;
import hotel.habitacion.HabitacionLibre;
import hotel.habitacion.TipoHabitacion;
import hotel.modelo.Cliente;
import hotel.reserva.EstadoReserva;
import hotel.reserva.EstadosReserva;
import hotel.reserva.RegistroHistorial;
import hotel.reserva.Reserva;
import hotel.reserva.ReservaBuilder;
import hotel.reserva.SolicitudReserva;
import hotel.tarifa.Cotizacion;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ReservaServiceImpl implements ReservaService {

    private final ReservaConsulta consulta;
    private final ReservaEscritura escritura;
    private final CanalConsulta canales;
    private final DisponibilidadConsulta disponibilidad;
    private final ClienteBusqueda clientes;
    private final TipoHabitacionConsulta tipos;
    private final CotizadorService cotizador;

    public ReservaServiceImpl(ReservaConsulta consulta, ReservaEscritura escritura,
            CanalConsulta canales, DisponibilidadConsulta disponibilidad,
            ClienteBusqueda clientes, TipoHabitacionConsulta tipos,
            CotizadorService cotizador) {
        this.consulta = consulta;
        this.escritura = escritura;
        this.canales = canales;
        this.disponibilidad = disponibilidad;
        this.clientes = clientes;
        this.tipos = tipos;
        this.cotizador = cotizador;
    }

    @Override
    public List<String> tiposHabitacion() {
        List<String> nombres = new ArrayList<>();
        for (TipoHabitacion t : tipos.listar()) {
            nombres.add(t.nombre());
        }
        return nombres;
    }

    @Override
    public List<Canal> canales() {
        return canales.listar();
    }

    @Override
    public List<String> estados() {
        return EstadosReserva.nombres();
    }

    @Override
    public List<HabitacionLibre> habitacionesDisponibles(String tipoHabitacion, LocalDate entrada,
            LocalDate salida) throws NegocioException {
        if (entrada == null || salida == null || !salida.isAfter(entrada)) {
            throw new NegocioException("Indique fechas válidas: la salida debe ser posterior a la entrada.");
        }
        TipoHabitacion tipo = buscarTipo(tipoHabitacion);
        return disponibilidad.buscar(tipo.id(), entrada, salida);
    }

    @Override
    public Reserva crear(SolicitudReserva s, Integer idUsuario) throws NegocioException {
        if (vacio(s.documentoCliente())) {
            throw new NegocioException("Indique el documento del cliente.");
        }
        if (s.adultos() < 1) {
            throw new NegocioException("Debe haber al menos 1 adulto.");
        }
        if (s.ninos() < 0) {
            throw new NegocioException("La cantidad de niños no puede ser negativa.");
        }
        Cliente cliente = clientes.buscarPorDocumento(s.documentoCliente().trim())
                .orElseThrow(() -> new NegocioException("No existe un cliente con ese documento."));
        TipoHabitacion tipo = buscarTipo(s.tipoHabitacion());
        Canal canal = buscarCanal(s.canal());

        int huespedes = s.adultos() + s.ninos();
        if (huespedes > tipo.capacidad()) {
            throw new NegocioException("La habitación " + tipo.nombre()
                    + " admite máximo " + tipo.capacidad() + " huésped(es).");
        }

        Cotizacion cotizacion = cotizador.cotizar(tipo.nombre(), s.entrada(), s.salida(),
                cliente.getDocumento(), s.codigoPromocion(), canal.nombre());

        HabitacionLibre habitacion = elegirHabitacion(
                disponibilidad.buscar(tipo.id(), s.entrada(), s.salida()), s.idHabitacion(), tipo);

        double descuento = Math.max(0,
                Math.round((tipo.tarifaBase() * cotizacion.noches() - cotizacion.total()) * 100.0) / 100.0);
        EstadoReserva estadoInicial = canal.confirmacionAutomatica()
                ? EstadosReserva.CONFIRMADA : EstadosReserva.PENDIENTE;

        Reserva nueva = new ReservaBuilder()
                .cliente(cliente.getIdCliente(), cliente.getNombres() + " " + cliente.getApellidos(),
                        cliente.getDocumento())
                .tipo(tipo.id(), tipo.nombre())
                .habitacion(habitacion.id(), habitacion.numero())
                .canal(canal.id(), canal.nombre())
                .fechas(s.entrada(), s.salida())
                .huespedes(s.adultos(), s.ninos())
                .precio(tipo.tarifaBase(), descuento, cotizacion.total())
                .promocion(cotizacion.idPromocion())
                .estado(estadoInicial)
                .observaciones(vacioANull(s.observaciones()))
                .build();
        return escritura.insertar(nueva, idUsuario);
    }

    @Override
    public double anticipoRequerido(Reserva reserva) {
        for (Canal c : canales.listar()) {
            if (c.id() == reserva.getIdCanal() && c.requiereAnticipo()) {
                return Math.round(reserva.getTotal() * c.porcentajeAnticipo()) / 100.0;
            }
        }
        return 0;
    }

    @Override
    public List<Reserva> listar(String estado) {
        return consulta.listar(vacio(estado) || "TODOS".equals(estado) ? null : estado);
    }

    @Override
    public List<RegistroHistorial> historial(int idReserva) {
        return consulta.historial(idReserva);
    }

    @Override
    public void confirmar(Reserva reserva, Integer idUsuario) throws NegocioException {
        reserva.confirmar();
        escritura.cambiarEstado(reserva.getId(), reserva.getEstado().nombre(), idUsuario,
                "Reserva confirmada");
    }

    @Override
    public boolean cancelar(Reserva reserva, Integer idUsuario) throws NegocioException {
        boolean conPenalidad = correspondePenalidad(reserva);
        reserva.cancelar();
        escritura.cambiarEstado(reserva.getId(), reserva.getEstado().nombre(), idUsuario,
                conPenalidad ? "Cancelada con penalidad" : "Cancelada sin penalidad");
        return conPenalidad;
    }

    @Override
    public void marcarNoShow(Reserva reserva, Integer idUsuario) throws NegocioException {
        reserva.marcarNoShow();
        escritura.cambiarEstado(reserva.getId(), reserva.getEstado().nombre(), idUsuario,
                "El huésped no se presentó");
    }

    private boolean correspondePenalidad(Reserva reserva) {
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), reserva.getFechaEntrada());
        for (Canal c : canales.listar()) {
            if (c.id() == reserva.getIdCanal()) {
                return dias < c.diasCancelacionGratis();
            }
        }
        return false;
    }

    private HabitacionLibre elegirHabitacion(List<HabitacionLibre> libres, Integer idElegida,
            TipoHabitacion tipo) throws NegocioException {
        if (idElegida == null) {
            if (libres.isEmpty()) {
                throw new NegocioException(
                        "No hay habitaciones " + tipo.nombre() + " disponibles en esas fechas.");
            }
            return libres.get(0);
        }
        for (HabitacionLibre h : libres) {
            if (h.id() == idElegida) {
                return h;
            }
        }
        throw new NegocioException("La habitación elegida no está disponible en esas fechas.");
    }

    private TipoHabitacion buscarTipo(String nombre) throws NegocioException {
        for (TipoHabitacion t : tipos.listar()) {
            if (t.nombre().equalsIgnoreCase(nombre)) {
                return t;
            }
        }
        throw new NegocioException("Tipo de habitación no encontrado: " + nombre);
    }

    private Canal buscarCanal(String nombre) throws NegocioException {
        for (Canal c : canales.listar()) {
            if (c.nombre().equalsIgnoreCase(nombre)) {
                return c;
            }
        }
        throw new NegocioException("Canal de reserva no encontrado: " + nombre);
    }

    private boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String vacioANull(String s) {
        return vacio(s) ? null : s.trim();
    }
}

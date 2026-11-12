package hotel.servicio;

import hotel.dao.DisponibilidadConsulta;
import hotel.dao.EstadiaConsulta;
import hotel.dao.EstadiaEscritura;
import hotel.dao.HabitacionConsulta;
import hotel.dao.ReservaConsulta;
import hotel.estadia.Alojamiento;
import hotel.estadia.Consumo;
import hotel.estadia.Cuenta;
import hotel.estadia.Estadia;
import hotel.estadia.GrupoCuenta;
import hotel.estadia.Servicio;
import hotel.excepcion.NegocioException;
import hotel.habitacion.EstadoHabitacion;
import hotel.habitacion.Habitacion;
import hotel.habitacion.HabitacionLibre;
import hotel.reserva.Reserva;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EstadiaServiceImpl implements EstadiaService {

    private static final String EN_CURSO = "EN_CURSO";

    private final ReservaConsulta reservas;
    private final DisponibilidadConsulta disponibilidad;
    private final HabitacionConsulta habitaciones;
    private final EstadiaConsulta consulta;
    private final EstadiaEscritura escritura;

    public EstadiaServiceImpl(ReservaConsulta reservas, DisponibilidadConsulta disponibilidad,
            HabitacionConsulta habitaciones, EstadiaConsulta consulta,
            EstadiaEscritura escritura) {
        this.reservas = reservas;
        this.disponibilidad = disponibilidad;
        this.habitaciones = habitaciones;
        this.consulta = consulta;
        this.escritura = escritura;
    }

    @Override
    public List<Reserva> llegadasDeHoy() {
        LocalDate hoy = LocalDate.now();
        List<Reserva> llegadas = new ArrayList<>();
        for (Reserva r : reservas.listar("CONFIRMADA")) {
            if (!hoy.isBefore(r.getFechaEntrada()) && hoy.isBefore(r.getFechaSalida())) {
                llegadas.add(r);
            }
        }
        return llegadas;
    }

    @Override
    public List<HabitacionLibre> habitacionesParaCheckIn(Reserva reserva) {
        Map<Integer, Habitacion> porId = new HashMap<>();
        for (Habitacion h : habitaciones.listar()) {
            porId.put(h.getId(), h);
        }
        List<HabitacionLibre> opciones = new ArrayList<>();
        Integer propia = reserva.getIdHabitacion();
        if (propia != null && listaParaUsar(porId.get(propia))) {
            Habitacion h = porId.get(propia);
            opciones.add(new HabitacionLibre(h.getId(), h.getNumero(), h.getPiso()));
        }
        for (HabitacionLibre libre : disponibilidad.buscar(reserva.getIdTipo(),
                reserva.getFechaEntrada(), reserva.getFechaSalida())) {
            boolean yaIncluida = propia != null && libre.id() == propia;
            if (!yaIncluida && listaParaUsar(porId.get(libre.id()))) {
                opciones.add(libre);
            }
        }
        return opciones;
    }

    @Override
    public int registrarCheckIn(Reserva reserva, int idHabitacion, Integer idUsuario)
            throws NegocioException {
        LocalDate hoy = LocalDate.now();
        if (hoy.isBefore(reserva.getFechaEntrada())) {
            throw new NegocioException("La reserva es para el " + reserva.getFechaEntrada()
                    + "; todavía no corresponde el check-in.");
        }
        if (!hoy.isBefore(reserva.getFechaSalida())) {
            throw new NegocioException("La fecha de salida de la reserva ya pasó.");
        }
        reserva.registrarCheckIn();
        return escritura.registrarCheckIn(reserva.getId(), idHabitacion, idUsuario);
    }

    @Override
    public List<Estadia> estadiasEnCurso() {
        return consulta.listarEnCurso();
    }

    @Override
    public List<Servicio> servicios() {
        return consulta.listarServicios();
    }

    @Override
    public void registrarConsumo(Estadia estadia, Servicio servicio, int cantidad, Integer idUsuario)
            throws NegocioException {
        if (cantidad < 1) {
            throw new NegocioException("La cantidad debe ser al menos 1.");
        }
        if (!EN_CURSO.equals(estadia.estado())) {
            throw new NegocioException("Solo se pueden registrar consumos en una estadía en curso.");
        }
        escritura.registrarConsumo(estadia.idEstadia(), servicio.idServicio(), cantidad, idUsuario);
    }

    @Override
    public Cuenta cuentaDe(Estadia estadia) {
        long noches = ChronoUnit.DAYS.between(estadia.fechaEntrada(), estadia.fechaSalida());
        GrupoCuenta cuenta = new GrupoCuenta("Cuenta " + estadia.codigoReserva() + " - " + estadia.cliente());
        cuenta.agregar(new Alojamiento("Alojamiento " + noches + " noche(s) - Hab. "
                + estadia.habitacion(), estadia.totalAlojamiento()));

        Map<String, GrupoCuenta> porArea = new LinkedHashMap<>();
        for (Consumo consumo : consulta.consumosDe(estadia.idEstadia())) {
            porArea.computeIfAbsent(consumo.area(), GrupoCuenta::new).agregar(consumo);
        }
        for (GrupoCuenta grupo : porArea.values()) {
            cuenta.agregar(grupo);
        }
        return new Cuenta(cuenta, consulta.anticiposPagados(estadia.idEstadia()));
    }

    private boolean listaParaUsar(Habitacion habitacion) {
        return habitacion != null && habitacion.getEstado() == EstadoHabitacion.DISPONIBLE;
    }
}

package hotel.modelo.reserva;

import hotel.excepcion.NegocioException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reserva {

    private final int id;
    private final String codigo;
    private final int idCliente;
    private final String cliente;
    private final String documento;
    private final int idTipo;
    private final String tipo;
    private final Integer idHabitacion;
    private final String habitacion;
    private final int idCanal;
    private final String canal;
    private final Integer idPromocion;
    private final Integer idPaquete;
    private final LocalDate fechaEntrada;
    private final LocalDate fechaSalida;
    private final int adultos;
    private final int ninos;
    private final double tarifaNoche;
    private final double descuento;
    private final double total;
    private final double anticipo;
    private final String observaciones;
    private EstadoReserva estado;

    Reserva(ReservaBuilder b) {
        this.id = b.id;
        this.codigo = b.codigo;
        this.idCliente = b.idCliente;
        this.cliente = b.cliente;
        this.documento = b.documento;
        this.idTipo = b.idTipo;
        this.tipo = b.tipo;
        this.idHabitacion = b.idHabitacion;
        this.habitacion = b.habitacion;
        this.idCanal = b.idCanal;
        this.canal = b.canal;
        this.idPromocion = b.idPromocion;
        this.idPaquete = b.idPaquete;
        this.fechaEntrada = b.fechaEntrada;
        this.fechaSalida = b.fechaSalida;
        this.adultos = b.adultos;
        this.ninos = b.ninos;
        this.tarifaNoche = b.tarifaNoche;
        this.descuento = b.descuento;
        this.total = b.total;
        this.anticipo = b.anticipo;
        this.observaciones = b.observaciones;
        this.estado = b.estado;
    }

    public void confirmar() throws NegocioException {
        estado = estado.confirmar();
    }

    public void cancelar() throws NegocioException {
        estado = estado.cancelar();
    }

    public void marcarNoShow() throws NegocioException {
        estado = estado.marcarNoShow();
    }

    public void registrarCheckIn() throws NegocioException {
        estado = estado.registrarCheckIn();
    }

    public void registrarCheckOut() throws NegocioException {
        estado = estado.registrarCheckOut();
    }

    public ReservaMemento crearMemento() {
        return new ReservaMemento(estado);
    }

    public void restaurar(ReservaMemento memento) {
        this.estado = memento.estado();
    }

    public int getNoches() {
        return (int) ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
    }

    public int getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public String getCliente() {
        return cliente;
    }

    public String getDocumento() {
        return documento;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public String getTipo() {
        return tipo;
    }

    public Integer getIdHabitacion() {
        return idHabitacion;
    }

    public String getHabitacion() {
        return habitacion;
    }

    public int getIdCanal() {
        return idCanal;
    }

    public String getCanal() {
        return canal;
    }

    public Integer getIdPromocion() {
        return idPromocion;
    }

    public Integer getIdPaquete() {
        return idPaquete;
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public int getAdultos() {
        return adultos;
    }

    public int getNinos() {
        return ninos;
    }

    public double getTarifaNoche() {
        return tarifaNoche;
    }

    public double getDescuento() {
        return descuento;
    }

    public double getTotal() {
        return total;
    }

    public double getAnticipo() {
        return anticipo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public EstadoReserva getEstado() {
        return estado;
    }
}

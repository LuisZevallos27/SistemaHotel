package hotel.reserva;

import java.time.LocalDate;

public class ReservaBuilder {

    int id;
    String codigo;
    int idCliente;
    String cliente;
    String documento;
    int idTipo;
    String tipo;
    Integer idHabitacion;
    String habitacion;
    int idCanal;
    String canal;
    Integer idPromocion;
    Integer idPaquete;
    LocalDate fechaEntrada;
    LocalDate fechaSalida;
    int adultos = 1;
    int ninos = 0;
    double tarifaNoche;
    double descuento;
    double total;
    double anticipo;
    String observaciones;
    EstadoReserva estado = EstadosReserva.PENDIENTE;

    public static ReservaBuilder desde(Reserva r) {
        return new ReservaBuilder()
                .id(r.getId())
                .codigo(r.getCodigo())
                .cliente(r.getIdCliente(), r.getCliente(), r.getDocumento())
                .tipo(r.getIdTipo(), r.getTipo())
                .habitacion(r.getIdHabitacion(), r.getHabitacion())
                .canal(r.getIdCanal(), r.getCanal())
                .promocion(r.getIdPromocion())
                .paquete(r.getIdPaquete())
                .fechas(r.getFechaEntrada(), r.getFechaSalida())
                .huespedes(r.getAdultos(), r.getNinos())
                .precio(r.getTarifaNoche(), r.getDescuento(), r.getTotal())
                .anticipo(r.getAnticipo())
                .observaciones(r.getObservaciones())
                .estado(r.getEstado());
    }

    public ReservaBuilder id(int id) {
        this.id = id;
        return this;
    }

    public ReservaBuilder codigo(String codigo) {
        this.codigo = codigo;
        return this;
    }

    public ReservaBuilder cliente(int idCliente, String nombre, String documento) {
        this.idCliente = idCliente;
        this.cliente = nombre;
        this.documento = documento;
        return this;
    }

    public ReservaBuilder tipo(int idTipo, String nombre) {
        this.idTipo = idTipo;
        this.tipo = nombre;
        return this;
    }

    public ReservaBuilder habitacion(Integer idHabitacion, String numero) {
        this.idHabitacion = idHabitacion;
        this.habitacion = numero;
        return this;
    }

    public ReservaBuilder canal(int idCanal, String nombre) {
        this.idCanal = idCanal;
        this.canal = nombre;
        return this;
    }

    public ReservaBuilder promocion(Integer idPromocion) {
        this.idPromocion = idPromocion;
        return this;
    }

    public ReservaBuilder paquete(Integer idPaquete) {
        this.idPaquete = idPaquete;
        return this;
    }

    public ReservaBuilder fechas(LocalDate entrada, LocalDate salida) {
        this.fechaEntrada = entrada;
        this.fechaSalida = salida;
        return this;
    }

    public ReservaBuilder huespedes(int adultos, int ninos) {
        this.adultos = adultos;
        this.ninos = ninos;
        return this;
    }

    public ReservaBuilder precio(double tarifaNoche, double descuento, double total) {
        this.tarifaNoche = tarifaNoche;
        this.descuento = descuento;
        this.total = total;
        return this;
    }

    public ReservaBuilder anticipo(double anticipo) {
        this.anticipo = anticipo;
        return this;
    }

    public ReservaBuilder estado(EstadoReserva estado) {
        this.estado = estado;
        return this;
    }

    public ReservaBuilder observaciones(String observaciones) {
        this.observaciones = observaciones;
        return this;
    }

    public Reserva build() {
        if (idCliente <= 0) {
            throw new IllegalStateException("La reserva requiere un cliente.");
        }
        if (idTipo <= 0) {
            throw new IllegalStateException("La reserva requiere un tipo de habitación.");
        }
        if (idCanal <= 0) {
            throw new IllegalStateException("La reserva requiere un canal.");
        }
        if (fechaEntrada == null || fechaSalida == null || !fechaSalida.isAfter(fechaEntrada)) {
            throw new IllegalStateException("La reserva requiere fechas válidas.");
        }
        if (adultos < 1 || ninos < 0) {
            throw new IllegalStateException("La reserva requiere al menos un adulto.");
        }
        if (estado == null) {
            throw new IllegalStateException("La reserva requiere un estado.");
        }
        return new Reserva(this);
    }
}

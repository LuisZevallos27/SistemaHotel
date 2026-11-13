package hotel.servicio;

public record Servicios(ClienteService clientes, HabitacionService habitaciones,
        ReservaService reservas, EstadiaService estadias,
        CheckOutService checkOut, CotizadorService cotizador,
        LimpiezaService limpieza, MantenimientoService mantenimiento) {

}

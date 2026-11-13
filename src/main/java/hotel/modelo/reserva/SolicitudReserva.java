package hotel.modelo.reserva;

import java.time.LocalDate;

public record SolicitudReserva(String documentoCliente, String tipoHabitacion, String canal,
        Integer idHabitacion, LocalDate entrada, LocalDate salida,
        int adultos, int ninos, String codigoPromocion, String observaciones) {

}

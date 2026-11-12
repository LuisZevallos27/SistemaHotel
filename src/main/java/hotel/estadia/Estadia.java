package hotel.estadia;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Estadia(int idEstadia, int idReserva, String codigoReserva, int idHabitacion,
        String habitacion, LocalDateTime fechaCheckIn, LocalDateTime fechaCheckOut,
        String estado, int idCliente, String cliente, String documento,
        LocalDate fechaEntrada, LocalDate fechaSalida, double tarifaNoche,
        double totalAlojamiento) {

}

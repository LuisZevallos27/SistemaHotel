package hotel.reserva;

import java.time.LocalDateTime;

public record RegistroHistorial(LocalDateTime fecha, String estadoAnterior, String estadoNuevo,
        String usuario, String detalle) {

}

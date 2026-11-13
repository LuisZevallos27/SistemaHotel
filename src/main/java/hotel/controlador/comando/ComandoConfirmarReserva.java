package hotel.controlador.comando;

import hotel.excepcion.NegocioException;
import hotel.modelo.reserva.Reserva;
import hotel.modelo.reserva.ReservaMemento;
import hotel.controlador.ReservaService;

public class ComandoConfirmarReserva implements ComandoReserva {

    private final ReservaService servicio;
    private final Reserva reserva;
    private final Integer idUsuario;
    private ReservaMemento memento;

    public ComandoConfirmarReserva(ReservaService servicio, Reserva reserva, Integer idUsuario) {
        this.servicio = servicio;
        this.reserva = reserva;
        this.idUsuario = idUsuario;
    }

    @Override
    public void ejecutar() throws NegocioException {
        memento = reserva.crearMemento();
        servicio.confirmar(reserva, idUsuario);
    }

    @Override
    public void deshacer() throws NegocioException {
        servicio.restaurarEstado(reserva, memento, idUsuario, "Se deshizo: " + descripcion());
    }

    @Override
    public String descripcion() {
        return "Confirmar " + reserva.getCodigo();
    }
}

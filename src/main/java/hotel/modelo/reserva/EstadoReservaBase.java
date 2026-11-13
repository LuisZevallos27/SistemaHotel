package hotel.modelo.reserva;

import hotel.excepcion.NegocioException;

public abstract class EstadoReservaBase implements EstadoReserva {

    private EstadoReserva rechazar(String accion) throws NegocioException {
        throw new NegocioException("No se puede " + accion + " una reserva en estado " + nombre() + ".");
    }

    @Override
    public EstadoReserva confirmar() throws NegocioException {
        return rechazar("confirmar");
    }

    @Override
    public EstadoReserva cancelar() throws NegocioException {
        return rechazar("cancelar");
    }

    @Override
    public EstadoReserva marcarNoShow() throws NegocioException {
        return rechazar("marcar como no-show");
    }

    @Override
    public EstadoReserva registrarCheckIn() throws NegocioException {
        return rechazar("registrar el check-in de");
    }

    @Override
    public EstadoReserva registrarCheckOut() throws NegocioException {
        return rechazar("registrar el check-out de");
    }

    @Override
    public boolean admiteModificacion() {
        return false;
    }

    @Override
    public boolean ocupaHabitacion() {
        return false;
    }
}

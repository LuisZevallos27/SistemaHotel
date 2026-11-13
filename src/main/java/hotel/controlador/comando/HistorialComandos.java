package hotel.controlador.comando;

import hotel.excepcion.NegocioException;
import java.util.ArrayDeque;
import java.util.Deque;

public class HistorialComandos {

    private static final int LIMITE = 20;

    private final Deque<ComandoReserva> ejecutados = new ArrayDeque<>();

    public void ejecutar(ComandoReserva comando) throws NegocioException {
        comando.ejecutar();
        ejecutados.push(comando);
        if (ejecutados.size() > LIMITE) {
            ejecutados.removeLast();
        }
    }

    public boolean puedeDeshacer() {
        return !ejecutados.isEmpty();
    }

    public String descripcionUltimo() {
        return ejecutados.isEmpty() ? "" : ejecutados.peek().descripcion();
    }

    public void deshacerUltimo() throws NegocioException {
        ComandoReserva ultimo = ejecutados.peek();
        if (ultimo == null) {
            return;
        }
        ultimo.deshacer();
        ejecutados.pop();
    }
}

package hotel.modelo.tarifa;

import java.time.LocalDate;

public class FabricaTarifaFidelidad implements FabricaTarifa {

    @Override
    public Tarifa decorar(Tarifa tarifa, ContextoTarifa contexto, LocalDate fecha) {
        Descuentos d = contexto.descuentos();
        if (d.descuentoNivel() > 0) {
            return new TarifaDescuento(tarifa, "Fidelidad " + d.nivel(), d.descuentoNivel());
        }
        return tarifa;
    }
}

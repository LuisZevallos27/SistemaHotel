package hotel.dao;

import hotel.modelo.tarifa.Descuentos;
import java.util.Optional;

public interface DescuentoClienteConsulta {

    Optional<Descuentos> buscarPorDocumento(String documento);
}

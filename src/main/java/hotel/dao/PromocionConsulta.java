package hotel.dao;

import hotel.tarifa.Promocion;
import java.time.LocalDate;
import java.util.Optional;

public interface PromocionConsulta {

    Optional<Promocion> buscarVigentePorCodigo(String codigo, LocalDate fecha);
}

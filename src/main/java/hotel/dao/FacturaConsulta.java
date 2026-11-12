package hotel.dao;

import hotel.facturacion.Factura;
import hotel.facturacion.LineaFactura;
import java.util.List;
import java.util.Optional;

public interface FacturaConsulta {

    Optional<Factura> vigentePorEstadia(int idEstadia);

    Optional<Factura> buscarPorId(int idFactura);

    List<LineaFactura> lineas(int idFactura);
}

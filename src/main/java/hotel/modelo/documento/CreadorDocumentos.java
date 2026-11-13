package hotel.modelo.documento;

import java.util.List;
import java.util.Optional;

public interface CreadorDocumentos {

    Optional<Documento> crear(String tipo, String numero);

    List<String> tipos();
}

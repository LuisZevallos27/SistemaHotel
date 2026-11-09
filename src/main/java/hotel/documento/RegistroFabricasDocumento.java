package hotel.documento;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RegistroFabricasDocumento implements CreadorDocumentos {

    private final Map<String, FabricaDocumento> fabricas = new LinkedHashMap<>();

    public RegistroFabricasDocumento() {
        registrar(new FabricaDni());
        registrar(new FabricaCarnetExtranjeria());
        registrar(new FabricaPasaporte());
        registrar(new FabricaRuc());
    }

    public final void registrar(FabricaDocumento fabrica) {
        fabricas.put(fabrica.tipo(), fabrica);
    }

    @Override
    public Optional<Documento> crear(String tipo, String numero) {
        FabricaDocumento fabrica = fabricas.get(tipo);
        if (fabrica == null) {
            return Optional.empty();
        }
        return Optional.of(fabrica.crear(numero));
    }

    @Override
    public List<String> tipos() {
        return new ArrayList<>(fabricas.keySet());
    }
}

package hotel.seguridad;

import java.util.Map;
import java.util.Set;

public record PoliticaAcceso(Map<String, Set<Modulo>> permisos) {

    public PoliticaAcceso {
        permisos = Map.copyOf(permisos);
    }

    public boolean permite(String rol, Modulo modulo) {
        Set<Modulo> modulos = permisos.get(rol);
        return modulos != null && modulos.contains(modulo);
    }
}

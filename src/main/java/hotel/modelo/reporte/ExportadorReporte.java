package hotel.modelo.reporte;

import java.io.IOException;
import java.nio.file.Path;

public interface ExportadorReporte {

    void exportar(String contenido, Path destino) throws IOException;
}

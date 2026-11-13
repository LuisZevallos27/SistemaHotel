package hotel.modelo.reporte;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExportadorTxt implements ExportadorReporte {

    @Override
    public void exportar(String contenido, Path destino) throws IOException {
        String normalizado = contenido.replace("\r\n", "\n")
                .replace("\n", System.lineSeparator());
        Files.writeString(destino, normalizado + System.lineSeparator(), StandardCharsets.UTF_8);
    }
}

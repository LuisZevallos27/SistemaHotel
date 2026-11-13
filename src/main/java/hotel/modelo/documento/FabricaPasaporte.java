package hotel.modelo.documento;

public class FabricaPasaporte implements FabricaDocumento {

    @Override
    public String tipo() {
        return "PASAPORTE";
    }

    @Override
    public Documento crear(String numero) {
        return new DocumentoPasaporte(numero);
    }
}

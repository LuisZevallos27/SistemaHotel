package hotel.modelo.documento;

public class FabricaRuc implements FabricaDocumento {

    @Override
    public String tipo() {
        return "RUC";
    }

    @Override
    public Documento crear(String numero) {
        return new DocumentoRuc(numero);
    }
}

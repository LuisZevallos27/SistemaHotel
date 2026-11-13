package hotel.modelo.documento;

public class FabricaCarnetExtranjeria implements FabricaDocumento {

    @Override
    public String tipo() {
        return "CE";
    }

    @Override
    public Documento crear(String numero) {
        return new DocumentoCarnetExtranjeria(numero);
    }
}

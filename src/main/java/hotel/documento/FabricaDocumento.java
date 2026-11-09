package hotel.documento;

public interface FabricaDocumento {

    String tipo();

    Documento crear(String numero);
}

package hotel.modelo.documento;

public interface FabricaDocumento {

    String tipo();

    Documento crear(String numero);
}

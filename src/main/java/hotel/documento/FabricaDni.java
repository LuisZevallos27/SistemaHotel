package hotel.documento;

public class FabricaDni implements FabricaDocumento {

    @Override
    public String tipo() {
        return "DNI";
    }

    @Override
    public Documento crear(String numero) {
        return new DocumentoDni(numero);
    }
}

package hotel.documento;

public class DocumentoDni extends Documento {

    public DocumentoDni(String numero) {
        super(numero);
    }

    @Override
    public String getTipo() {
        return "DNI";
    }

    @Override
    public boolean esValido() {
        return getNumero().matches("\\d{8}");
    }

    @Override
    public String mensajeError() {
        return "El DNI debe tener exactamente 8 dígitos.";
    }
}

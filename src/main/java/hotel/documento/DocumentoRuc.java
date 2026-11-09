package hotel.documento;

public class DocumentoRuc extends Documento {

    public DocumentoRuc(String numero) {
        super(numero);
    }

    @Override
    public String getTipo() {
        return "RUC";
    }

    @Override
    public boolean esValido() {
        return getNumero().matches("\\d{11}");
    }

    @Override
    public String mensajeError() {
        return "El RUC debe tener exactamente 11 dígitos.";
    }
}

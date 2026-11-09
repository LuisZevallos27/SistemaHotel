package hotel.documento;

public class DocumentoCarnetExtranjeria extends Documento {

    public DocumentoCarnetExtranjeria(String numero) {
        super(numero);
    }

    @Override
    public String getTipo() {
        return "CE";
    }

    @Override
    public boolean esValido() {
        return getNumero().matches("[A-Za-z0-9]{9,12}");
    }

    @Override
    public String mensajeError() {
        return "El carné de extranjería debe tener entre 9 y 12 caracteres alfanuméricos.";
    }
}

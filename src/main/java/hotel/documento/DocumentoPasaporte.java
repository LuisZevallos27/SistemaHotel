package hotel.documento;

public class DocumentoPasaporte extends Documento {

    public DocumentoPasaporte(String numero) {
        super(numero);
    }

    @Override
    public String getTipo() {
        return "PASAPORTE";
    }

    @Override
    public boolean esValido() {
        return getNumero().matches("[A-Za-z0-9]{6,12}");
    }

    @Override
    public String mensajeError() {
        return "El pasaporte debe tener entre 6 y 12 caracteres alfanuméricos.";
    }
}

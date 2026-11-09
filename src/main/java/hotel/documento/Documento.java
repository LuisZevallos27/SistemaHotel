package hotel.documento;

public abstract class Documento {

    private final String numero;

    protected Documento(String numero) {
        this.numero = numero == null ? "" : numero.trim();
    }

    public String getNumero() {
        return numero;
    }

    public abstract String getTipo();

    public abstract boolean esValido();

    public abstract String mensajeError();
}

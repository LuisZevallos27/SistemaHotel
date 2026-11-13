package hotel.operacion;

public record Empleado(int id, String nombre, String rol, boolean activo) {

    @Override
    public String toString() {
        return nombre;
    }
}

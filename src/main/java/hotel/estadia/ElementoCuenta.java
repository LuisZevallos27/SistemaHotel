package hotel.estadia;

import java.util.List;

public interface ElementoCuenta {

    String descripcion();

    double total();

    List<ElementoCuenta> hijos();
}

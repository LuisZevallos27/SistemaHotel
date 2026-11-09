package hotel.config;

import java.sql.Connection;
import java.sql.SQLException;

public interface ProveedorConexion {

    Connection getConexion() throws SQLException;
}

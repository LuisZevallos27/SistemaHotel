package hotel.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionBD implements ProveedorConexion {

    private static ConexionBD instancia;
    private final String url;
    private final String usuario;
    private final String clave;

    private ConexionBD() {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream("db.properties")) {
            p.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se encontró db.properties en la raíz del proyecto (junto al pom.xml).", e);
        }

        String host = p.getProperty("db.host", "localhost").trim();
        String instanciaSql = p.getProperty("db.instance", "").trim();
        String puerto = p.getProperty("db.port", instanciaSql.isEmpty() ? "1433" : "").trim();
        String bd = p.getProperty("db.name", "HotelDB").trim();

        this.usuario = p.getProperty("db.user", "").trim();
        this.clave = p.getProperty("db.password", "").trim();

        StringBuilder sb = new StringBuilder("jdbc:sqlserver://").append(host);
        if (!puerto.isEmpty()) {
            sb.append(":").append(puerto);
        }
        if (!instanciaSql.isEmpty()) {
            sb.append(";instanceName=").append(instanciaSql);
        }
        sb.append(";databaseName=").append(bd)
                .append(";encrypt=true;trustServerCertificate=true");
        this.url = sb.toString();
    }

    public static synchronized ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    @Override
    public Connection getConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }
}

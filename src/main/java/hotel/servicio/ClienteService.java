package hotel.servicio;

import hotel.dao.ClienteDAO;
import hotel.modelo.Cliente;
import java.sql.SQLException;
import java.util.List;

public class ClienteService {

    private final ClienteDAO dao;

    // La dependencia entra por constructor: el servicio depende de la interfaz, no de SQL Server.
    public ClienteService(ClienteDAO dao) {
        this.dao = dao;
    }

    public List<Cliente> listar() throws SQLException {
        return dao.listar();
    }

    public void guardar(Cliente c) throws NegocioException, SQLException {
        validar(c);
        c.setTelefono(vacioANull(c.getTelefono()));
        c.setCorreo(vacioANull(c.getCorreo()));
        c.setNacionalidad(vacioANull(c.getNacionalidad()));
        if (c.getIdCliente() == 0) {
            dao.insertar(c);
        } else {
            dao.actualizar(c);
        }
    }

    public void desactivar(int idCliente) throws SQLException {
        dao.desactivar(idCliente);
    }

    private void validar(Cliente c) throws NegocioException, SQLException {
        String doc = c.getDocumento() == null ? "" : c.getDocumento();
        boolean documentoValido;
        switch (String.valueOf(c.getTipoDocumento())) {
            case "DNI":
                documentoValido = doc.matches("\\d{8}");
                break;
            case "RUC":
                documentoValido = doc.matches("\\d{11}");
                break;
            case "CE":
                documentoValido = doc.matches("[A-Za-z0-9]{9,12}");
                break;
            case "PASAPORTE":
                documentoValido = doc.matches("[A-Za-z0-9]{6,12}");
                break;
            default:
                documentoValido = false;
        }
        if (!documentoValido) {
            throw new NegocioException("El documento no es válido para el tipo " + c.getTipoDocumento() + ".");
        }
        if (vacio(c.getNombres())) {
            throw new NegocioException("Los nombres son obligatorios.");
        }
        if (vacio(c.getApellidos())) {
            throw new NegocioException("Los apellidos son obligatorios.");
        }
        if (!vacio(c.getCorreo()) && !c.getCorreo().contains("@")) {
            throw new NegocioException("El correo no es válido.");
        }
        if (dao.existeDocumento(doc, c.getIdCliente())) {
            throw new NegocioException("Ya existe un cliente con ese documento.");
        }
    }

    private boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String vacioANull(String s) {
        return vacio(s) ? null : s.trim();
    }
}

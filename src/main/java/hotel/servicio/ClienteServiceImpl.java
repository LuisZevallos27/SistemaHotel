package hotel.servicio;

import hotel.dao.ClienteConsulta;
import hotel.dao.ClienteEscritura;
import hotel.documento.CreadorDocumentos;
import hotel.documento.Documento;
import hotel.excepcion.NegocioException;
import hotel.modelo.Cliente;
import java.util.List;

public class ClienteServiceImpl implements ClienteService {

    private final ClienteConsulta consulta;
    private final ClienteEscritura escritura;
    private final CreadorDocumentos documentos;

    public ClienteServiceImpl(ClienteConsulta consulta, ClienteEscritura escritura,
            CreadorDocumentos documentos) {
        this.consulta = consulta;
        this.escritura = escritura;
        this.documentos = documentos;
    }

    @Override
    public List<Cliente> listar() {
        return consulta.listar();
    }

    @Override
    public void guardar(Cliente c) throws NegocioException {
        validar(c);
        c.setTelefono(vacioANull(c.getTelefono()));
        c.setCorreo(vacioANull(c.getCorreo()));
        c.setNacionalidad(vacioANull(c.getNacionalidad()));
        if (c.getIdCliente() == 0) {
            escritura.insertar(c);
        } else {
            escritura.actualizar(c);
        }
    }

    @Override
    public void desactivar(int idCliente) {
        escritura.desactivar(idCliente);
    }

    @Override
    public List<String> tiposDocumento() {
        return documentos.tipos();
    }

    private void validar(Cliente c) throws NegocioException {
        Documento documento = documentos.crear(c.getTipoDocumento(), c.getDocumento())
                .orElseThrow(() -> new NegocioException(
                "Tipo de documento no soportado: " + c.getTipoDocumento()));
        if (!documento.esValido()) {
            throw new NegocioException(documento.mensajeError());
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
        if (consulta.existeDocumento(documento.getNumero(), c.getIdCliente())) {
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

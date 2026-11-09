package hotel;

import hotel.config.ConexionBD;
import hotel.config.ProveedorConexion;
import hotel.dao.ClienteDAOImpl;
import hotel.documento.RegistroFabricasDocumento;
import hotel.servicio.ClienteService;
import hotel.servicio.ClienteServiceImpl;
import hotel.vista.FrmClientes;
import javax.swing.SwingUtilities;

public class SistemaHotel {

    public static void main(String[] args) {
        ProveedorConexion conexion = ConexionBD.getInstancia();

        ClienteDAOImpl clienteDao = new ClienteDAOImpl(conexion);
        ClienteService clientes = new ClienteServiceImpl(
                clienteDao, clienteDao, new RegistroFabricasDocumento());

        SwingUtilities.invokeLater(() -> new FrmClientes(clientes).setVisible(true));
    }
}

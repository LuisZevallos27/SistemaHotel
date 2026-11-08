package hotel;

import hotel.config.ConexionBD;
import hotel.dao.ClienteDAO;
import hotel.dao.ClienteDAOImpl;
import hotel.servicio.ClienteService;
import hotel.vista.FrmClientes;
import javax.swing.SwingUtilities;

public class SistemaHotel {

    public static void main(String[] args) {
        ClienteDAO dao = new ClienteDAOImpl(ConexionBD.getInstancia());
        ClienteService servicio = new ClienteService(dao);
        SwingUtilities.invokeLater(() -> new FrmClientes(servicio).setVisible(true));
    }
}
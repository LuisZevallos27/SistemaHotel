package hotel;

import hotel.vista.FrmClientes;
import javax.swing.SwingUtilities;

public class SistemaHotel {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmClientes().setVisible(true));
    }
}

package hotel.dao;

import hotel.modelo.estadia.Consumo;
import hotel.modelo.estadia.Estadia;
import hotel.modelo.estadia.Servicio;
import java.util.List;

public interface EstadiaConsulta {

    List<Estadia> listarEnCurso();

    List<Servicio> listarServicios();

    List<Consumo> consumosDe(int idEstadia);

    double anticiposPagados(int idEstadia);
}

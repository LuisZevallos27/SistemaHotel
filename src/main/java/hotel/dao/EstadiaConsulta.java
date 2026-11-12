package hotel.dao;

import hotel.estadia.Consumo;
import hotel.estadia.Estadia;
import hotel.estadia.Servicio;
import java.util.List;

public interface EstadiaConsulta {

    List<Estadia> listarEnCurso();

    List<Servicio> listarServicios();

    List<Consumo> consumosDe(int idEstadia);

    double anticiposPagados(int idEstadia);
}

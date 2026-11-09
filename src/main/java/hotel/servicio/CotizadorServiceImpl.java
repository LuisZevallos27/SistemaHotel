package hotel.servicio;

import hotel.dao.DescuentoClienteConsulta;
import hotel.dao.PromocionConsulta;
import hotel.dao.TemporadaConsulta;
import hotel.dao.TipoHabitacionConsulta;
import hotel.excepcion.NegocioException;
import hotel.habitacion.TipoHabitacion;
import hotel.tarifa.ContextoTarifa;
import hotel.tarifa.Cotizacion;
import hotel.tarifa.Descuentos;
import hotel.tarifa.FabricaTarifa;
import hotel.tarifa.Promocion;
import hotel.tarifa.Tarifa;
import hotel.tarifa.TarifaBase;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CotizadorServiceImpl implements CotizadorService {

    private final TipoHabitacionConsulta tipos;
    private final TemporadaConsulta temporadas;
    private final PromocionConsulta promociones;
    private final DescuentoClienteConsulta descuentos;
    private final List<FabricaTarifa> fabricas;

    public CotizadorServiceImpl(TipoHabitacionConsulta tipos, TemporadaConsulta temporadas,
            PromocionConsulta promociones, DescuentoClienteConsulta descuentos,
            List<FabricaTarifa> fabricas) {
        this.tipos = tipos;
        this.temporadas = temporadas;
        this.promociones = promociones;
        this.descuentos = descuentos;
        this.fabricas = fabricas;
    }

    @Override
    public List<String> tiposHabitacion() {
        List<String> nombres = new ArrayList<>();
        for (TipoHabitacion t : tipos.listar()) {
            nombres.add(t.nombre());
        }
        return nombres;
    }

    @Override
    public Cotizacion cotizar(String tipoHabitacion, LocalDate entrada, LocalDate salida,
            String documentoCliente, String codigoPromocion) throws NegocioException {
        if (entrada == null || salida == null) {
            throw new NegocioException("Indique las fechas de entrada y salida.");
        }
        if (entrada.isBefore(LocalDate.now())) {
            throw new NegocioException("La fecha de entrada no puede ser anterior a hoy.");
        }
        if (!salida.isAfter(entrada)) {
            throw new NegocioException("La salida debe ser posterior a la entrada.");
        }
        int noches = (int) ChronoUnit.DAYS.between(entrada, salida);

        TipoHabitacion tipo = buscarTipo(tipoHabitacion);
        Descuentos descuentosCliente = buscarDescuentos(documentoCliente);
        Promocion promocion = buscarPromocion(codigoPromocion, entrada, noches, tipo);

        ContextoTarifa contexto = new ContextoTarifa(
                tipo, noches, descuentosCliente, promocion, temporadas.listar());

        double total = 0;
        List<String> porNoche = new ArrayList<>();
        List<String> calculo = new ArrayList<>();
        for (LocalDate fecha = entrada; fecha.isBefore(salida); fecha = fecha.plusDays(1)) {
            Tarifa tarifa = new TarifaBase(tipo.tarifaBase());
            for (FabricaTarifa fabrica : fabricas) {
                tarifa = fabrica.decorar(tarifa, contexto, fecha);
            }
            total += tarifa.precioNoche();
            porNoche.add(String.format(Locale.US, "%s: S/ %.2f", fecha, tarifa.precioNoche()));
            if (calculo.isEmpty()) {
                calculo = tarifa.detalle();
            }
        }
        return new Cotizacion(tipo.nombre(), entrada, salida, noches, calculo, porNoche,
                Math.round(total * 100.0) / 100.0);
    }

    private TipoHabitacion buscarTipo(String nombre) throws NegocioException {
        for (TipoHabitacion t : tipos.listar()) {
            if (t.nombre().equalsIgnoreCase(nombre)) {
                return t;
            }
        }
        throw new NegocioException("Tipo de habitación no encontrado: " + nombre);
    }

    private Descuentos buscarDescuentos(String documento) throws NegocioException {
        if (vacio(documento)) {
            return Descuentos.NINGUNO;
        }
        return descuentos.buscarPorDocumento(documento.trim())
                .orElseThrow(() -> new NegocioException("No existe un cliente con ese documento."));
    }

    private Promocion buscarPromocion(String codigo, LocalDate entrada, int noches,
            TipoHabitacion tipo) throws NegocioException {
        if (vacio(codigo)) {
            return null;
        }
        Promocion promocion = promociones.buscarVigentePorCodigo(codigo.trim(), entrada)
                .orElseThrow(() -> new NegocioException(
                "El código de promoción no existe o no está vigente."));
        if (noches < promocion.minNoches()) {
            throw new NegocioException(
                    "La promoción requiere al menos " + promocion.minNoches() + " noches.");
        }
        if (promocion.idTipo() != null && promocion.idTipo() != tipo.id()) {
            throw new NegocioException(
                    "La promoción no aplica al tipo de habitación " + tipo.nombre() + ".");
        }
        return promocion;
    }

    private boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }
}

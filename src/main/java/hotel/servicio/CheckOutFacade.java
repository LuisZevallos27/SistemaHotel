package hotel.servicio;

import hotel.dao.ClienteBusqueda;
import hotel.dao.EstadiaEscritura;
import hotel.dao.FacturaConsulta;
import hotel.dao.FacturaEscritura;
import hotel.dao.PagoEscritura;
import hotel.estadia.Cuenta;
import hotel.estadia.ElementoCuenta;
import hotel.estadia.Estadia;
import hotel.excepcion.NegocioException;
import hotel.facturacion.Factura;
import hotel.facturacion.LineaFactura;
import hotel.facturacion.MetodoPago;
import hotel.facturacion.NuevaFactura;
import hotel.facturacion.ResultadoCheckOut;
import hotel.facturacion.TipoComprobante;
import hotel.modelo.Cliente;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Facade: ofrece una sola operación de check-out y oculta la coordinación entre
 * la cuenta, la facturación, el cobro y el cierre de la estadía.
 */
public class CheckOutFacade implements CheckOutService {

    private static final double IGV = 0.18;
    private static final String EN_CURSO = "EN_CURSO";
    private static final String PAGADA = "PAGADA";

    private final EstadiaService estadias;
    private final ClienteBusqueda clientes;
    private final FacturaConsulta facturas;
    private final FacturaEscritura emision;
    private final PagoEscritura pagos;
    private final EstadiaEscritura cierre;

    public CheckOutFacade(EstadiaService estadias, ClienteBusqueda clientes, FacturaConsulta facturas,
            FacturaEscritura emision, PagoEscritura pagos, EstadiaEscritura cierre) {
        this.estadias = estadias;
        this.clientes = clientes;
        this.facturas = facturas;
        this.emision = emision;
        this.pagos = pagos;
        this.cierre = cierre;
    }

    @Override
    public ResultadoCheckOut finalizar(Estadia estadia, TipoComprobante tipo, MetodoPago metodo,
            String referencia, Integer idUsuario) throws NegocioException {
        if (!EN_CURSO.equals(estadia.estado())) {
            throw new NegocioException("La estadía ya fue finalizada.");
        }
        Cliente cliente = clientes.buscarPorDocumento(estadia.documento())
                .orElseThrow(() -> new NegocioException("No se encontró al cliente de la estadía."));
        if (tipo == TipoComprobante.FACTURA && cliente.getIdEmpresa() == null) {
            throw new NegocioException("Solo los clientes con empresa (convenio) pueden recibir factura.");
        }
        if (metodo == MetodoPago.CREDITO && cliente.getIdEmpresa() == null) {
            throw new NegocioException("El pago a crédito es solo para clientes con empresa (convenio).");
        }

        Cuenta cuenta = estadias.cuentaDe(estadia);
        Factura factura = obtenerFactura(estadia, cliente, tipo, cuenta, idUsuario);

        double pagado = 0;
        double saldo = cuenta.saldo();
        if (!PAGADA.equals(factura.estado()) && saldo > 0) {
            pagos.registrarPagoFactura(factura.idFactura(), metodo, saldo, referencia, idUsuario);
            pagado = saldo;
        }

        cierre.registrarCheckOut(estadia.idEstadia(), idUsuario);

        Factura emitida = facturas.buscarPorId(factura.idFactura()).orElse(factura);
        return new ResultadoCheckOut(emitida, facturas.lineas(emitida.idFactura()),
                pagado, cuenta.anticipos());
    }

    @Override
    public void registrarEncuesta(Estadia estadia, int puntaje, String comentario)
            throws NegocioException {
        if (puntaje < 1 || puntaje > 5) {
            throw new NegocioException("El puntaje debe estar entre 1 y 5.");
        }
        cierre.registrarEncuesta(estadia.idEstadia(), puntaje,
                comentario == null || comentario.trim().isEmpty() ? null : comentario.trim());
    }

    private Factura obtenerFactura(Estadia estadia, Cliente cliente, TipoComprobante tipo,
            Cuenta cuenta, Integer idUsuario) throws NegocioException {
        Optional<Factura> vigente = facturas.vigentePorEstadia(estadia.idEstadia());
        if (vigente.isPresent()) {
            Factura existente = vigente.get();
            boolean igual = existente.tipo() == tipo
                    && Math.abs(existente.total() - cuenta.total()) < 0.005;
            if (igual) {
                return existente;
            }
            emision.anular(existente.idFactura());
        }

        double total = cuenta.total();
        double subtotal = redondear(total / (1 + IGV));
        double igv = redondear(total - subtotal);
        Integer idEmpresa = tipo == TipoComprobante.FACTURA ? cliente.getIdEmpresa() : null;

        int idFactura = emision.emitir(new NuevaFactura(estadia.idEstadia(), cliente.getIdCliente(),
                idEmpresa, tipo, subtotal, igv, total, idUsuario, lineasDe(cuenta)));
        return facturas.buscarPorId(idFactura)
                .orElseThrow(() -> new NegocioException("No se pudo recuperar la factura emitida."));
    }

    private List<LineaFactura> lineasDe(Cuenta cuenta) {
        List<LineaFactura> lineas = new ArrayList<>();
        agregarHojas(cuenta.detalle(), lineas);
        return lineas;
    }

    private void agregarHojas(ElementoCuenta elemento, List<LineaFactura> lineas) {
        if (elemento.hijos().isEmpty()) {
            lineas.add(new LineaFactura(elemento.descripcion(), 1, elemento.total()));
            return;
        }
        for (ElementoCuenta hijo : elemento.hijos()) {
            agregarHojas(hijo, lineas);
        }
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}

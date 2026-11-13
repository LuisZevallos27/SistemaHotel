package hotel.reporte;

import java.util.List;

public final class CatalogoReportes {

    private CatalogoReportes() {
    }

    public static List<DefinicionReporte> todos() {
        ParametroReporte desde = new ParametroReporte("Desde", TipoParametro.FECHA, false, "");
        ParametroReporte hasta = new ParametroReporte("Hasta", TipoParametro.FECHA, false, "");

        return List.of(
                new DefinicionReporte("Ocupación actual",
                        "Cantidad de habitaciones por estado en este momento.",
                        "usp_Reporte_OcupacionActual",
                        List.of("Estado", "Habitaciones", "% del total"),
                        List.of(1, 2), List.of()),
                new DefinicionReporte("Ingresos por tipo de habitación",
                        "Facturas pagadas e ingresos por tipo de habitación. Filtro por fecha de emisión.",
                        "usp_Reporte_IngresosPorTipo",
                        List.of("Tipo de habitación", "Facturas", "Ingresos (S/)"),
                        List.of(1, 2), List.of(desde, hasta)),
                new DefinicionReporte("Demanda por mes",
                        "Reservas y noches por mes (sin las canceladas). Filtro por año de entrada.",
                        "usp_Reporte_DemandaPorMes",
                        List.of("Año", "Mes", "Reservas", "Noches"),
                        List.of(2, 3),
                        List.of(new ParametroReporte("Año", TipoParametro.ENTERO, false, ""))),
                new DefinicionReporte("Servicios más consumidos",
                        "Unidades e ingresos de los servicios adicionales. Filtro por fecha de consumo.",
                        "usp_Reporte_ServiciosMasConsumidos",
                        List.of("Servicio", "Área", "Unidades", "Ingresos (S/)"),
                        List.of(2, 3), List.of(desde, hasta)),
                new DefinicionReporte("Satisfacción por tipo de habitación",
                        "Puntaje promedio de las encuestas por tipo de habitación.",
                        "usp_Reporte_SatisfaccionPorTipo",
                        List.of("Tipo de habitación", "Encuestas", "Puntaje promedio"),
                        List.of(1), List.of()),
                new DefinicionReporte("Reservas por canal",
                        "Reservas (sin las canceladas) y monto por canal. Filtro por fecha de entrada.",
                        "usp_Reporte_ReservasPorCanal",
                        List.of("Canal", "Reservas", "Total (S/)"),
                        List.of(1, 2), List.of(desde, hasta)),
                new DefinicionReporte("Reservas por estado",
                        "Cantidad de reservas en cada estado.",
                        "usp_Reporte_ReservasPorEstado",
                        List.of("Estado", "Reservas"),
                        List.of(1), List.of()),
                new DefinicionReporte("Clientes frecuentes",
                        "Clientes con más reservas confirmadas o realizadas.",
                        "usp_Reporte_ClientesFrecuentes",
                        List.of("Documento", "Cliente", "Reservas", "Puntos", "Nivel"),
                        List.of(),
                        List.of(new ParametroReporte("Cantidad de clientes", TipoParametro.ENTERO, true, "10")))
        );
    }
}

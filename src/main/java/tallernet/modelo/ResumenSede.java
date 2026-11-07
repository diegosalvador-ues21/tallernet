package tallernet.modelo;

import java.math.BigDecimal;

/** Fila del reporte consolidado por sede (Q3). Inmutable. */
public record ResumenSede(String sede, int turnosConfirmados, int minutosAgendados,
                          BigDecimal ingresoEstimado) {
}

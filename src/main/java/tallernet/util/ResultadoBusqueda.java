package tallernet.util;

/**
 * Resultado de una búsqueda: posición encontrada (-1 si no está) y comparaciones realizadas.
 * Se modela como record (Java 16+): un tipo inmutable cuyos atributos son privados y finales.
 */
public record ResultadoBusqueda(int indice, int comparaciones) {

    public boolean encontrado() {
        return indice >= 0;
    }
}

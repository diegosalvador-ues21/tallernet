package tallernet.util;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/** Algoritmos de búsqueda lineal y binaria, genéricos sobre la clave de búsqueda. */
public final class Busqueda {

    private Busqueda() {
    }

    /** Búsqueda secuencial: O(n), no requiere que la lista esté ordenada. */
    public static <T, K> ResultadoBusqueda lineal(List<T> lista, K clave, Function<T, K> extractor) {
        int comparaciones = 0;
        for (int i = 0; i < lista.size(); i++) {
            comparaciones++;
            if (extractor.apply(lista.get(i)).equals(clave)) {
                return new ResultadoBusqueda(i, comparaciones);
            }
        }
        return new ResultadoBusqueda(-1, comparaciones);
    }

    /**
     * Búsqueda binaria: O(log n). Precondición: la lista está ordenada por la misma clave
     * y con el mismo comparador. En cada paso descarta la mitad donde la clave no puede estar.
     */
    public static <T, K> ResultadoBusqueda binaria(List<T> ordenada, K clave, Function<T, K> extractor,
                                                   Comparator<? super K> comparador) {
        int bajo = 0;
        int alto = ordenada.size() - 1;
        int comparaciones = 0;
        while (bajo <= alto) {
            int medio = (bajo + alto) >>> 1;
            int resultado = comparador.compare(extractor.apply(ordenada.get(medio)), clave);
            comparaciones++;
            if (resultado == 0) {
                return new ResultadoBusqueda(medio, comparaciones);
            } else if (resultado < 0) {
                bajo = medio + 1;
            } else {
                alto = medio - 1;
            }
        }
        return new ResultadoBusqueda(-1, comparaciones);
    }
}

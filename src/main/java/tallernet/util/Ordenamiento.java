package tallernet.util;

import java.util.Comparator;
import java.util.List;

/**
 * Algoritmos de ordenamiento implementados sin recurrir a {@code Collections.sort},
 * para mostrar su funcionamiento. Son genéricos: el criterio lo aporta un {@link Comparator}.
 * Cada método devuelve la cantidad de comparaciones realizadas, que la vista informa
 * y las pruebas usan para contrastar la complejidad.
 */
public final class Ordenamiento {

    private Ordenamiento() {
    }

    /**
     * Ordenamiento por inserción. O(n²) en el peor caso y O(n) si la lista ya está casi
     * ordenada; es estable. Conviene para listas chicas, como la agenda de un día.
     */
    public static <T> long insercion(List<T> lista, Comparator<? super T> criterio) {
        long comparaciones = 0;
        for (int i = 1; i < lista.size(); i++) {
            T actual = lista.get(i);
            int j = i - 1;
            while (j >= 0) {
                comparaciones++;
                if (criterio.compare(lista.get(j), actual) <= 0) {
                    break;
                }
                lista.set(j + 1, lista.get(j));
                j--;
            }
            lista.set(j + 1, actual);
        }
        return comparaciones;
    }

    /**
     * Quicksort con pivote central (esquema de Lomuto sobre el pivote movido al final).
     * O(n log n) en promedio y O(n²) en el peor caso; no es estable.
     * Se usa para el catálogo de servicios y el listado completo de clientes. Con muchas claves
     * repetidas el esquema de Lomuto se acerca al peor caso; con los volúmenes del taller no incide.
     */
    public static <T> long quicksort(List<T> lista, Comparator<? super T> criterio) {
        long[] comparaciones = {0};
        quicksort(lista, criterio, 0, lista.size() - 1, comparaciones);
        return comparaciones[0];
    }

    private static <T> void quicksort(List<T> lista, Comparator<? super T> criterio, int desde, int hasta,
                                      long[] comparaciones) {
        if (desde >= hasta) {
            return;
        }
        int medio = desde + (hasta - desde) / 2;
        intercambiar(lista, medio, hasta);
        T pivote = lista.get(hasta);
        int i = desde;
        for (int j = desde; j < hasta; j++) {
            comparaciones[0]++;
            if (criterio.compare(lista.get(j), pivote) < 0) {
                intercambiar(lista, i, j);
                i++;
            }
        }
        intercambiar(lista, i, hasta);
        quicksort(lista, criterio, desde, i - 1, comparaciones);
        quicksort(lista, criterio, i + 1, hasta, comparaciones);
    }

    private static <T> void intercambiar(List<T> lista, int a, int b) {
        if (a != b) {
            T aux = lista.get(a);
            lista.set(a, lista.get(b));
            lista.set(b, aux);
        }
    }
}

package tallernet.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Ordenamiento y búsqueda propios, contrastados con la biblioteca estándar. */
@DisplayName("Algoritmos de ordenamiento y búsqueda")
class AlgoritmosTest {

    private static List<Integer> aleatorios(int n, long semilla) {
        Random r = new Random(semilla);
        List<Integer> lista = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            lista.add(r.nextInt(10_000));
        }
        return lista;
    }

    @Test
    @DisplayName("Inserción y quicksort producen el mismo orden que List.sort")
    void ordenanIgualQueLaBiblioteca() {
        for (int n : new int[] {0, 1, 2, 17, 500}) {
            List<Integer> esperado = aleatorios(n, n);
            List<Integer> a = new ArrayList<>(esperado);
            List<Integer> b = new ArrayList<>(esperado);
            esperado.sort(Comparator.naturalOrder());
            Ordenamiento.insercion(a, Comparator.naturalOrder());
            Ordenamiento.quicksort(b, Comparator.naturalOrder());
            assertEquals(esperado, a);
            assertEquals(esperado, b);
        }
    }

    @Test
    @DisplayName("Inserción sobre una lista ya ordenada hace n-1 comparaciones")
    void insercionMejorCaso() {
        List<Integer> ordenada = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8));
        assertEquals(7, Ordenamiento.insercion(ordenada, Comparator.naturalOrder()));
    }

    @Test
    @DisplayName("Búsqueda binaria: encuentra con a lo sumo log2(n)+1 comparaciones")
    void busquedaBinaria() {
        List<String> patentes = new ArrayList<>(List.of("OKL482", "AE123BC", "AF789GH", "AC456DE", "AD321JK"));
        Ordenamiento.quicksort(patentes, Comparator.naturalOrder());
        ResultadoBusqueda r = Busqueda.binaria(patentes, "AF789GH", p -> p, Comparator.naturalOrder());
        assertTrue(r.encontrado());
        assertEquals("AF789GH", patentes.get(r.indice()));
        assertTrue(r.comparaciones() <= 3);
        assertFalse(Busqueda.binaria(patentes, "ZZ999ZZ", p -> p, Comparator.naturalOrder()).encontrado());
    }

    @Test
    @DisplayName("Búsqueda lineal recorre toda la lista cuando la clave no existe")
    void busquedaLineal() {
        List<String> patentes = List.of("OKL482", "AE123BC", "AF789GH");
        ResultadoBusqueda r = Busqueda.lineal(patentes, "ZZ999ZZ", p -> p);
        assertFalse(r.encontrado());
        assertEquals(3, r.comparaciones());
    }
}

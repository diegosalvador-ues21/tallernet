package tallernet.propiedades;

import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;
import tallernet.modelo.AgendaDiaria;
import tallernet.modelo.Fixtures;
import tallernet.modelo.Turno;
import tallernet.util.Busqueda;
import tallernet.util.Ordenamiento;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/** CPP01 - Pruebas basadas en propiedades (jqwik): invariantes que deben valer para cualquier dato. */
class PropiedadesTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 11, 9);

    @Provide
    Arbitrary<Turno> turnos() {
        Arbitrary<Integer> minuto = Arbitraries.integers().between(0, 23).map(i -> i * 30);   // 08:00 a 19:30
        Arbitrary<Integer> duracion = Arbitraries.of(30, 60, 90, 180, 240);
        Arbitrary<Boolean> otraSede = Arbitraries.of(true, false);
        return net.jqwik.api.Combinators.combine(minuto, duracion, otraSede).as((m, d, s) -> {
            try {
                return new Turno(s ? Fixtures.flores() : Fixtures.echeandia(), Fixtures.gol(),
                        Fixtures.servicio(1, d, null), 2, LocalDateTime.of(LUNES, LocalTime.of(8, 0).plusMinutes(m)), null);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @Property(tries = 1000)
    void laSuperposicionEsSimetrica(@ForAll("turnos") Turno a, @ForAll("turnos") Turno b) {
        assertThat(a.seSuperponeCon(b)).isEqualTo(b.seSuperponeCon(a));
    }

    @Property(tries = 1000)
    void turnosDeDistintaSedeNuncaSeSuperponen(@ForAll("turnos") Turno a, @ForAll("turnos") Turno b) {
        if (a.getSede().getId() != b.getSede().getId()) {
            assertThat(a.seSuperponeCon(b)).isFalse();
        }
    }

    @Property(tries = 500)
    void laGrillaNuncaSaleDeRangoYCuentaBien(@ForAll @Size(max = 12) List<@IntRange(min = 0, max = 23) Integer> inicios) {
        List<Turno> lista = new ArrayList<>();
        for (int i : inicios) {
            try {
                lista.add(new Turno(Fixtures.echeandia(), Fixtures.gol(), Fixtures.servicio(1, 90, null), 2,
                        LocalDateTime.of(LUNES, LocalTime.of(8, 0).plusMinutes(i * 30L)), null));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        AgendaDiaria agenda = new AgendaDiaria(Fixtures.echeandia(), LUNES, lista);
        assertThat(agenda.getCantidadFranjas()).isEqualTo(24);
        assertThat(agenda.contarOcupadas()).isBetween(0, 24);
        // Un hueco sugerido siempre está libre en todas sus franjas.
        agenda.primerHuecoLibre(60, LocalTime.of(8, 0)).ifPresent(h -> {
            int i = (int) java.time.Duration.between(LocalTime.of(8, 0), h).toMinutes() / 30;
            assertThat(agenda.estaLibre(i)).isTrue();
            assertThat(agenda.estaLibre(i + 1)).isTrue();
        });
    }

    @Property(tries = 500)
    void losOrdenamientosProducenUnaPermutacionOrdenada(@ForAll @Size(max = 60) List<Integer> datos) {
        List<Integer> esperado = new ArrayList<>(datos);
        esperado.sort(Comparator.naturalOrder());
        List<Integer> a = new ArrayList<>(datos);
        List<Integer> b = new ArrayList<>(datos);
        Ordenamiento.insercion(a, Comparator.naturalOrder());
        Ordenamiento.quicksort(b, Comparator.naturalOrder());
        assertThat(a).isEqualTo(esperado);
        assertThat(b).isEqualTo(esperado);
    }

    @Property(tries = 500)
    void laBusquedaBinariaCoincideConLaLineal(@ForAll @Size(max = 60) List<@IntRange(min = 0, max = 100) Integer> datos,
                                              @ForAll @IntRange(min = 0, max = 100) int clave) {
        List<Integer> ordenada = new ArrayList<>(new java.util.TreeSet<>(datos));
        boolean binaria = Busqueda.binaria(ordenada, clave, Function.identity(), Comparator.naturalOrder()).encontrado();
        boolean lineal = Busqueda.lineal(ordenada, clave, Function.identity()).encontrado();
        assertThat(binaria).isEqualTo(lineal).isEqualTo(ordenada.contains(clave));
    }
}

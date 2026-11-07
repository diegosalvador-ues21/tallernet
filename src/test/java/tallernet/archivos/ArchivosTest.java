package tallernet.archivos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tallernet.excepciones.ArchivoException;
import tallernet.modelo.AgendaDiaria;
import tallernet.modelo.Cliente;
import tallernet.modelo.Fixtures;
import tallernet.modelo.ReporteSemanal;
import tallernet.modelo.Turno;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** CPC09 - Archivos: exportación CSV/TXT, importación CSV y bitácora. */
@DisplayName("CPC09 - Exportación, importación y bitácora")
class ArchivosTest {

    @TempDir
    Path carpeta;

    @Test
    @DisplayName("la agenda se exporta en UTF-8 con BOM, encabezado y una fila por franja")
    void exportarAgenda() throws Exception {
        Turno t = new Turno(Fixtures.echeandia(), Fixtures.gol(), Fixtures.serviceCompleto(), 2,
                LocalDateTime.of(2026, 10, 5, 10, 0), null);
        t.setId(2);
        AgendaDiaria agenda = new AgendaDiaria(Fixtures.echeandia(), LocalDate.of(2026, 10, 5), List.of(t));
        Path archivo = new AgendaCsv().exportar(agenda, carpeta.resolve("exportaciones"));

        assertEquals("agenda_echeandia_2026-10-05.csv", archivo.getFileName().toString());
        List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
        assertEquals(25, lineas.size());
        assertTrue(lineas.get(0).startsWith("﻿Sede;Fecha;Franja"));
        assertTrue(lineas.get(1).startsWith("Sede Echeandía;05/10/2026;08:00;Libre"));
        assertEquals("Sede Echeandía;05/10/2026;10:00;Ocupada;2;AE123BC;Servicio de prueba 3;10:00;13:00",
                lineas.get(5));
    }

    @Test
    @DisplayName("un campo con punto y coma o comillas se escapa entre comillas")
    void escape() {
        assertEquals("\"Revisar; urgente\"", ExportadorCsv.escapar("Revisar; urgente"));
        assertEquals("\"Dice \"\"ruido\"\"\"", ExportadorCsv.escapar("Dice \"ruido\""));
        assertEquals("simple", ExportadorCsv.escapar("simple"));
    }

    @Test
    @DisplayName("el reporte semanal se exporta con nombre por semana ISO")
    void exportarReporte() throws Exception {
        ReporteSemanal r = new ReporteSemanal(LocalDate.of(2026, 10, 7), List.of(Fixtures.echeandia()), List.of());
        Path archivo = new ReporteSemanalTxt().exportar(r, carpeta);
        assertEquals("reporte_semanal_2026-S41.txt", archivo.getFileName().toString());
        assertTrue(Files.readString(archivo, StandardCharsets.UTF_8).contains("Semana del 05/10/2026 al 10/10/2026"));
    }

    @Test
    @DisplayName("importación: registra las líneas válidas e informa cada rechazo con su número de línea")
    void importarClientes() throws Exception {
        Path csv = carpeta.resolve("clientes.csv");
        Files.writeString(csv, "﻿nombre;apellido;telefono;email\n"
                + "Ana;Quiroga;11-4000-1000;ana@mail.com\n"
                + "Bruno;Paz;123;\n"
                + "Carla;Ibáñez;11-4000-3000\n"
                + "\n"
                + "Diego;Rivas;11 4000 4000;\n", StandardCharsets.UTF_8);
        int[] siguienteId = {100};
        ImportadorClientesCsv importador = new ImportadorClientesCsv((n, a, t, e) -> {
            Cliente c = new Cliente(n, a, t, e);
            c.setId(siguienteId[0]++);
            return c;
        });
        ImportadorClientesCsv.Resultado r = importador.importar(csv);
        assertEquals(4, r.lineasLeidas());
        assertEquals(2, r.registrados().size());
        assertEquals("Rivas, Diego", r.registrados().get(1).getNombreCompleto());
        assertEquals(2, r.rechazos().size());
        assertTrue(r.rechazos().get(0).startsWith("Línea 3 (Paz, Bruno): El teléfono"));
        assertTrue(r.rechazos().get(1).startsWith("Línea 4: se esperaban 4 columnas y hay 3"));
    }

    @Test
    @DisplayName("importar un archivo inexistente lanza ArchivoException con un mensaje claro")
    void archivoInexistente() {
        ImportadorClientesCsv importador = new ImportadorClientesCsv((n, a, t, e) -> null);
        ArchivoException e = assertThrows(ArchivoException.class, () -> importador.importar(carpeta.resolve("no.csv")));
        assertTrue(e.getMessage().startsWith("No existe el archivo"));
    }

    @Test
    @DisplayName("bitácora: agrega líneas y devuelve las últimas con un búfer circular")
    void bitacora() throws Exception {
        Clock fijo = Clock.fixed(Instant.parse("2026-10-01T11:00:00Z"), ZoneId.of("America/Argentina/Buenos_Aires"));
        Bitacora b = new Bitacora(carpeta.resolve("logs/tallernet.log"), fijo);
        b.setPuesto("Medina, Carla (sede 1)");
        for (int i = 1; i <= 7; i++) {
            b.registrar("ALTA_TURNO", "Turno N.° " + i);
        }
        List<String> ultimas = b.ultimas(3);
        assertEquals(3, ultimas.size());
        assertEquals("2026-10-01 08:00:00 | Medina, Carla (sede 1) | ALTA_TURNO | Turno N.° 5", ultimas.get(0));
        assertTrue(ultimas.get(2).endsWith("Turno N.° 7"));
        assertEquals(7, b.ultimas(50).size());
        assertTrue(new Bitacora(carpeta.resolve("vacia.log"), fijo).ultimas(5).isEmpty());
    }

    @Test
    @DisplayName("si la bitácora no puede escribirse, la operación sigue y queda un aviso")
    void bitacoraNoInterrumpe() throws Exception {
        Path comoArchivo = carpeta.resolve("ocupado");
        Files.writeString(comoArchivo, "x");
        Bitacora b = new Bitacora(comoArchivo.resolve("tallernet.log"), Clock.systemDefaultZone());
        b.registrar("ALTA_TURNO", "Turno N.° 1");
        assertTrue(b.getUltimoError().startsWith("No se pudo escribir la bitácora"));
    }
}

package tallernet.arquitectura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import tallernet.excepciones.TallerNetException;

/**
 * CPR01 - Reglas de arquitectura verificadas sobre el bytecode (ArchUnit): las decisiones del AP2
 * (MVC + DAO) no pueden degradarse sin que una prueba falle.
 */
@AnalyzeClasses(packages = "tallernet", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaTest {

    @ArchTest
    static final ArchRule capas = layeredArchitecture().consideringOnlyDependenciesInLayers()
            .layer("Vista").definedBy("tallernet.vista..")
            .layer("Controlador").definedBy("tallernet.controlador..")
            .layer("Archivos").definedBy("tallernet.archivos..")
            .layer("Persistencia").definedBy("tallernet.persistencia..")
            .layer("Conexion").definedBy("tallernet.conexion..")
            .layer("Modelo").definedBy("tallernet.modelo..")
            .whereLayer("Vista").mayNotBeAccessedByAnyLayer()
            .whereLayer("Controlador").mayOnlyBeAccessedByLayers("Vista")
            .whereLayer("Persistencia").mayOnlyBeAccessedByLayers("Controlador")
            .whereLayer("Conexion").mayOnlyBeAccessedByLayers("Persistencia", "Controlador");

    @ArchTest
    static final ArchRule sqlSoloEnPersistencia = noClasses()
            .that().resideOutsideOfPackages("tallernet.persistencia..", "tallernet.conexion..")
            .should().dependOnClassesThat().resideInAPackage("java.sql..")
            .because("el acceso a MySQL está encapsulado en los DAO (patrón DAO, AP2 2.1)");

    @ArchTest
    static final ArchRule modeloIndependiente = noClasses().that().resideInAPackage("tallernet.modelo..")
            .should().dependOnClassesThat().resideInAnyPackage("tallernet.vista..", "tallernet.controlador..",
                    "tallernet.persistencia..", "tallernet.conexion..", "tallernet.archivos..");

    @ArchTest
    static final ArchRule sinCiclos = slices().matching("tallernet.(*)..").should().beFreeOfCycles();

    @ArchTest
    static final ArchRule excepcionesPropias = classes().that().resideInAPackage("tallernet.excepciones..")
            .should().beAssignableTo(TallerNetException.class);

    @ArchTest
    static final ArchRule nombresDeDao = classes().that().resideInAPackage("tallernet.persistencia..")
            .and().areNotInterfaces().and().haveSimpleNameNotStartingWith("DaoJdbc")
            .and().haveSimpleNameNotStartingWith("Traductor").and().areNotNestedClasses()
            .should().haveSimpleNameEndingWith("DAO");

    @ArchTest
    static final ArchRule controladoresConSufijo = classes().that().resideInAPackage("tallernet.controlador..")
            .and().areTopLevelClasses().and().haveSimpleNameNotStartingWith("Validador")
            .should().haveSimpleNameEndingWith("Controlador");
}

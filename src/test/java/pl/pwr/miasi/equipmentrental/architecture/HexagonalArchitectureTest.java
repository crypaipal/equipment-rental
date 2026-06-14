package pl.pwr.miasi.equipmentrental.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class HexagonalArchitectureTest {

    private static final String BASE_PACKAGE = "pl.pwr.miasi.equipmentrental";

    private final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages(BASE_PACKAGE);

    @Test
    void domainLayerDoesNotDependOnFrameworks() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta..",
                        "javax..",
                        "org.hibernate.."
                )
                .check(importedClasses);
    }

    @Test
    void domainLayerDoesNotDependOnInfrastructureLayer() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(importedClasses);
    }

    @Test
    void applicationLayerDoesNotDependOnInfrastructureLayer() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(importedClasses);
    }

    @Test
    void boundedContextCoresDoNotDependOnOtherBoundedContexts() {
        noClasses()
                .that().resideInAnyPackage("..identity.domain..", "..identity.application..")
                .should().dependOnClassesThat().resideInAnyPackage("..inventory..", "..rental..")
                .check(importedClasses);

        noClasses()
                .that().resideInAnyPackage("..inventory.domain..", "..inventory.application..")
                .should().dependOnClassesThat().resideInAnyPackage("..identity..", "..rental..")
                .check(importedClasses);

        noClasses()
                .that().resideInAnyPackage("..rental.domain..", "..rental.application..")
                .should().dependOnClassesThat().resideInAnyPackage("..identity..", "..inventory..")
                .check(importedClasses);
    }

    @Test
    void inboundPortsAreInterfaces() {
        classes()
                .that().resideInAPackage("..application.port.in..")
                .should().beInterfaces()
                .check(importedClasses);
    }

    @Test
    void outboundRepositoryAndAccessPortsAreInterfaces() {
        classes()
                .that().resideInAPackage("..application.port.out..")
                .and().haveSimpleNameEndingWith("Repository")
                .should().beInterfaces()
                .check(importedClasses);

        classes()
                .that().resideInAPackage("..application.port.out..")
                .and().haveSimpleNameEndingWith("Port")
                .should().beInterfaces()
                .check(importedClasses);
    }

    @Test
    void restControllersStayInRestAdapters() {
        classes()
                .that().areAnnotatedWith(RestController.class)
                .should().resideInAPackage("..infrastructure..")
                .check(importedClasses);
    }

    @Test
    void jpaEntitiesStayInPersistenceAdapters() {
        classes()
                .that().haveSimpleNameEndingWith("JpaEntity")
                .should().resideInAPackage("..infrastructure.persistence..")
                .check(importedClasses);
    }
}

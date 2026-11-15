package com.estudiobdd.arquitetura;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Regras das cinco camadas, verificadas a cada "mvn test". */
@AnalyzeClasses(packages = "com.estudiobdd", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    @ArchTest
    static final ArchRule controllersNaoAcessamRepositorios = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAPackage("..repository..")
            .because("controllers devem passar pelo serviço (camada de serviço aplica regras e transações)");

    @ArchTest
    static final ArchRule repositoriosNaoDependemDeServicosNemControllers = noClasses()
            .that().resideInAPackage("..repository..")
            .should().dependOnClassesThat().resideInAnyPackage("..service..", "..controller..");

    @ArchTest
    static final ArchRule dominioNaoDependeDeOutrasCamadas = noClasses()
            .that().resideInAPackage("..model..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..service..", "..controller..", "..repository..", "..dto..");

    @ArchTest
    static final ArchRule servicosNaoDependemDeControllers = noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat().resideInAPackage("..controller..");
}

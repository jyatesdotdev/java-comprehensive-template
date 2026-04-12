package com.example.template.testing.archunit;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * ArchUnit tests enforce architectural constraints at compile time.
 * These run as regular unit tests and fail the build if rules are violated.
 */
@AnalyzeClasses(
        packages = "com.example.template.testing",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureRulesTest {

    @ArchTest
    static final ArchRule services_should_not_depend_on_each_other =
            noClasses().that().resideInAPackage("..service..")
                    .should().dependOnClassesThat().resideInAPackage("..service..")
                    .as("Services should not depend on other services directly");

    @ArchTest
    static final ArchRule repository_should_not_depend_on_service =
            noClasses().that().resideInAPackage("..repository..")
                    .should().dependOnClassesThat().resideInAPackage("..service..")
                    .as("Repositories must not depend on services");

    @ArchTest
    static final ArchRule no_cycles_between_packages =
            slices().matching("com.example.template.testing.(*)..")
                    .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule model_classes_should_be_records_or_have_no_setters =
            classes().that().resideInAPackage("..model..")
                    .should().beRecords()
                    .as("Model classes should be records (immutable)");
}

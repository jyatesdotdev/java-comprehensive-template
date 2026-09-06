package com.example.template.restfulapi.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * ArchUnit rules for the REST example students copy: controllers stay off persistence, and this
 * module does not depend on sibling template packages.
 */
@AnalyzeClasses(
    packages = "com.example.template.restfulapi",
    importOptions = ImportOption.DoNotIncludeTests.class)
@SuppressWarnings(
    "PMD.TestClassWithoutTestCases") // ArchUnit uses @ArchTest fields, not @Test methods
class ArchitectureRulesTest {

  @ArchTest
  static final ArchRule controllers_must_not_depend_on_persistence =
      noClasses()
          .that()
          .resideInAPackage("..controller..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..repository..",
              "..jdbc..",
              "..persistence..",
              "jakarta.persistence..",
              "javax.persistence..",
              "org.springframework.data..")
          .because("controllers talk to the service interface, never persistence");

  @ArchTest
  static final ArchRule restful_api_stays_in_its_package =
      noClasses()
          .that()
          .resideInAPackage("com.example.template.restfulapi..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "com.example.template.database..",
              "com.example.template.etl..",
              "com.example.template.hpc..",
              "com.example.template.patterns..",
              "com.example.template.simulation..",
              "com.example.template.systems..",
              "com.example.template.testing..")
          .because("copy-paste REST code must stay inside com.example.template.restfulapi");

  @ArchTest
  static final ArchRule no_cycles_between_packages =
      slices()
          .matching("com.example.template.restfulapi.(*)..")
          .should()
          .beFreeOfCycles()
          .because("layered packages must not cycle");
}

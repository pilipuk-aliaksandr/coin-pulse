package by.pilipuk.coinpulse.archunit;

import by.pilipuk.coinpulse.CoinPulseApp;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JODATIME;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(
        packagesOf = CoinPulseApp.class,
        importOptions = ImportOption.DoNotIncludeTests.class
)
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public final class CoinPulseArchitectureTest {

    /**
     * Пакеты не должны образовывать циклические зависимости.
     */
    @ArchTest
    static final ArchRule NO_CYCLIC_DEPENDENCIES =
            slices()
                    .matching("by.pilipuk.coinpulse.(*)..")
                    .should().beFreeOfCycles();

    @ArchTest
    public static final ArchRule NO_FIELD_INJECTION = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

    @ArchTest
    public static final ArchRule NO_STANDARD_STREAMS = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

    @ArchTest
    public static final ArchRule NO_JAVA_UTIL_LOGGING = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

    @ArchTest
    public static final ArchRule NO_USE_JODATIME = NO_CLASSES_SHOULD_USE_JODATIME;

    @ArchTest
    public static final ArchRule NO_THROW_GENERIC_EXCEPTIONS = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

    /**
     * Направление зависимостей между основными слоями.
     */
    @ArchTest
    static final ArchRule LAYERS_RESPECT_DEPENDENCY_DIRECTION =
            layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()

                    .layer("Controller").definedBy("..controller..")
                    .layer("Service").definedBy("..service..")
                    .layer("Repository").definedBy("..repository..")

                    .whereLayer("Controller").mayOnlyAccessLayers("Service")
                    .whereLayer("Service").mayOnlyAccessLayers("Repository");

    /**
     * Controller не должен обращаться к Repository напрямую.
     */
    @ArchTest
    static final ArchRule CONTROLLERS_DO_NOT_ACCESS_REPOSITORIES =
            noClasses()
                    .that().resideInAPackage("..controller..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..repository..");

    /**
     * Entity не должны зависеть от HTTP и Web API.
     */
    @ArchTest
    static final ArchRule ENTITIES_DO_NOT_DEPEND_ON_WEB =
            noClasses()
                    .that().areAnnotatedWith(Entity.class)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework.web..",
                            "org.springframework.http..",
                            "jakarta.servlet.."
                    );

    /**
     * Денежные значения не должны храниться в float/double.
     */
    @ArchTest
    static final ArchRule MONEY_DOES_NOT_USE_FLOATING_POINT =
            noFields()
                    .should().haveRawType(double.class)
                    .orShould().haveRawType(Double.class)
                    .orShould().haveRawType(float.class)
                    .orShould().haveRawType(Float.class);

    private CoinPulseArchitectureTest() {
    }
}

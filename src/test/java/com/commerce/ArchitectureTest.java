package com.commerce;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.Architectures;
import com.tngtech.archunit.library.Architectures.LayeredArchitecture;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * Checks the code against the layer maps of {@code docs/partition.yml}: the map is the one source, so adding a hexagon
 * needs no change here. A rule that matches no class passes.
 */
class ArchitectureTest {

    private static final String SOURCE_ROOT = "src/main/java/";

    private static JavaClasses classes;
    private static List<Hexagon> hexagons;

    @BeforeAll
    static void load() throws IOException {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.commerce");
        hexagons = readHexagons(Path.of("docs/partition.yml"));
    }

    @Test
    void every_layer_depends_only_on_the_layers_its_map_allows() {
        for (Hexagon hexagon : hexagons) {
            LayeredArchitecture architecture = Architectures.layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .withOptionalLayers(true);
            for (Map.Entry<String, String> layer : hexagon.layers().entrySet()) {
                architecture = architecture.layer(layer.getKey()).definedBy(layer.getValue());
            }
            for (String layer : hexagon.layers().keySet()) {
                String[] accessors = hexagon.accessorsOf(layer);
                architecture = accessors.length == 0
                        ? architecture.whereLayer(layer).mayNotBeAccessedByAnyLayer()
                        : architecture.whereLayer(layer).mayOnlyBeAccessedByLayers(accessors);
            }
            architecture.allowEmptyShould(true).check(classes);
        }
    }

    @Test
    void the_domain_of_a_rich_hexagon_imports_nothing_from_outside_itself() {
        for (Hexagon hexagon : hexagons) {
            String domain = hexagon.layer("domain");
            if (!"rich".equals(hexagon.form()) || domain == null) {
                continue;
            }
            classes().that().resideInAPackage(domain)
                    .should().onlyDependOnClassesThat().resideInAnyPackage(domain, "java..")
                    .allowEmptyShould(true)
                    .check(classes);
        }
    }

    /** No hexagon imports from another, except the types of a contract it declares, listed in {@link #CONTRACTS}. */
    @Test
    void no_hexagon_imports_from_another() {
        for (Hexagon hexagon : hexagons) {
            for (Hexagon other : hexagons) {
                if (hexagon == other) {
                    continue;
                }
                Set<String> contractTypes = CONTRACTS.stream()
                        .filter(c -> c.from().equals(hexagon.id()) && c.to().equals(other.id()))
                        .flatMap(c -> c.types().stream())
                        .collect(Collectors.toSet());
                noClasses().that().resideInAPackage(hexagon.rootPackage())
                        .should().dependOnClassesThat(JavaClass.Predicates.resideInAPackage(other.rootPackage())
                                .and(DescribedPredicate.describe("not a type of a declared contract",
                                        (JavaClass type) -> !contractTypes.contains(type.getName()))))
                        .allowEmptyShould(true)
                        .check(classes);
            }
        }
    }

    /** The layer map gives way for a declared contract from one layer only: the rest of the hexagon never reaches the other. */
    @Test
    void only_the_layer_a_contract_names_reaches_the_other_hexagon() {
        for (Contract contract : CONTRACTS) {
            Hexagon from = hexagon(contract.from());
            Hexagon to = hexagon(contract.to());
            noClasses().that().resideInAPackage(from.rootPackage())
                    .and().resideOutsideOfPackage(from.layer(contract.fromLayer()))
                    .should().dependOnClassesThat().resideInAPackage(to.rootPackage())
                    .allowEmptyShould(true)
                    .check(classes);
        }
    }

    /**
     * The contracts in process between hexagons, as {@code Contracts with neighbours} of both specs declare them
     * ({@code docs/specs/billing/payment/spec.md}, {@code docs/specs/billing/invoice/spec.md},
     * {@code docs/specs/billing/refund/spec.md}, {@code docs/specs/catalog/product/spec.md},
     * {@code docs/specs/catalog/price-list/spec.md}); {@code docs/partition.yml} declares no dependency between contexts
     * (architect, 2026-10-19).
     */
    private static final List<Contract> CONTRACTS = List.of(
            new Contract("payment/payment", "outbound", "invoice/invoice",
                    Set.of("com.commerce.invoice.application.InvoicePayments",
                            "com.commerce.invoice.application.InvoiceView",
                            "com.commerce.invoice.application.LineData",
                            "com.commerce.invoice.application.InvoiceNotIssued")),
            new Contract("refund/refund", "outbound", "payment/payment",
                    Set.of("com.commerce.payment.application.PaymentRefunds",
                            "com.commerce.payment.application.RefundableInvoice",
                            "com.commerce.payment.application.PaidPayment")),
            new Contract("product/product", "outbound", "price-list/price-list",
                    Set.of("com.commerce.pricelist.application.PriceListPrices",
                            "com.commerce.pricelist.application.PriceListPrice")));

    private record Contract(String from, String fromLayer, String to, Set<String> types) {
    }

    private static Hexagon hexagon(String id) {
        return hexagons.stream().filter(h -> h.id().equals(id)).findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private static List<Hexagon> readHexagons(Path file) throws IOException {
        Map<String, Object> config;
        try (Reader reader = Files.newBufferedReader(file)) {
            config = new Yaml().load(reader);
        }
        List<Hexagon> result = new ArrayList<>();
        for (Map<String, Object> context : (List<Map<String, Object>>) config.getOrDefault("contexts", List.of())) {
            for (Map<String, Object> hexagon : (List<Map<String, Object>>) context.getOrDefault("hexagons", List.of())) {
                String id = context.get("id") + "/" + hexagon.get("id");
                Map<String, String> layers = new LinkedHashMap<>();
                ((Map<String, String>) hexagon.get("layers"))
                        .forEach((layer, glob) -> layers.put(id + "/" + layer, toPackage(glob)));
                List<String[]> allowed = new ArrayList<>();
                for (String line : (List<String>) hexagon.getOrDefault("allowed", List.of())) {
                    String[] ends = line.split("->");
                    allowed.add(new String[] {id + "/" + ends[0].trim(), id + "/" + ends[1].trim()});
                }
                result.add(new Hexagon(id, (String) hexagon.get("form"), layers, allowed));
            }
        }
        return result;
    }

    /** {@code src/main/java/com/commerce/invoice/domain/**} becomes {@code com.commerce.invoice.domain..}. */
    private static String toPackage(String glob) {
        String path = glob.startsWith(SOURCE_ROOT) ? glob.substring(SOURCE_ROOT.length()) : glob;
        path = path.replaceAll("/\\*\\*$", "");
        return path.replace('/', '.') + "..";
    }

    private record Hexagon(String id, String form, Map<String, String> layers, List<String[]> allowed) {

        String layer(String name) {
            return layers.get(id + "/" + name);
        }

        String[] accessorsOf(String layer) {
            return allowed.stream().filter(edge -> edge[1].equals(layer)).map(edge -> edge[0]).toArray(String[]::new);
        }

        String rootPackage() {
            List<String> packages = layers.values().stream().map(p -> p.substring(0, p.length() - 2)).toList();
            String root = packages.getFirst();
            for (String p : packages) {
                while (!(p + ".").startsWith(root + ".")) {
                    root = root.substring(0, Math.max(root.lastIndexOf('.'), 0));
                }
            }
            return root + "..";
        }
    }
}

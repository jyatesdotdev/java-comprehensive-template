package com.example.template.patterns.structural;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.template.patterns.structural.StructuralPatterns.DataService;
import com.example.template.patterns.structural.StructuralPatterns.Decorators;
import com.example.template.patterns.structural.StructuralPatterns.FileSystemEntry;
import com.example.template.patterns.structural.StructuralPatterns.LegacyPrinter;
import com.example.template.patterns.structural.StructuralPatterns.OrderFacade;
import com.example.template.patterns.structural.StructuralPatterns.Printer;
import com.example.template.patterns.structural.StructuralPatterns.RealDataService;
import com.example.template.patterns.structural.StructuralPatterns.TextProcessor;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for the structural GoF pattern implementations. */
@DisplayName("Structural patterns")
class StructuralPatternsTest {

  @Nested
  @DisplayName("Adapter (legacy printer behind functional interface)")
  class AdapterPattern {

    @Test
    @DisplayName("adapted printer delegates to the legacy left-padding format")
    void adaptedPrinterDelegates() {
      Printer printer = StructuralPatterns.adapt(new LegacyPrinter(), 10);

      assertThat(printer.print("hi")).isEqualTo("hi        ").hasSize(10);
    }

    @Test
    @DisplayName("adapter honours the configured width")
    void adapterHonoursWidth() {
      Printer narrow = StructuralPatterns.adapt(new LegacyPrinter(), 5);
      Printer exact = StructuralPatterns.adapt(new LegacyPrinter(), 3);

      assertThat(narrow.print("ab")).isEqualTo("ab   ");
      assertThat(exact.print("abc")).isEqualTo("abc");
    }
  }

  @Nested
  @DisplayName("Decorator (composable text processors)")
  class DecoratorPattern {

    @Test
    @DisplayName("individual decorators transform text")
    void individualDecorators() {
      assertThat(Decorators.trimming().process("  hi  ")).isEqualTo("hi");
      assertThat(Decorators.upperCase().process("hi")).isEqualTo("HI");
      assertThat(Decorators.prefix(">> ").process("hi")).isEqualTo(">> hi");
      assertThat(Decorators.suffix(" <<").process("hi")).isEqualTo("hi <<");
    }

    @Test
    @DisplayName("andThen applies decorators in declaration order")
    void andThenAppliesInOrder() {
      TextProcessor trimThenUpper = Decorators.trimming().andThen(Decorators.upperCase());

      assertThat(trimThenUpper.process("  hello  ")).isEqualTo("HELLO");
    }

    @Test
    @DisplayName("pipeline composes many decorators left to right")
    void pipelineComposes() {
      TextProcessor pipeline =
          Decorators.pipeline(
              Decorators.trimming(),
              Decorators.upperCase(),
              Decorators.prefix("[ "),
              Decorators.suffix(" ]"));

      assertThat(pipeline.process("  hello world  ")).isEqualTo("[ HELLO WORLD ]");
    }

    @Test
    @DisplayName("empty pipeline is the identity")
    void emptyPipelineIsIdentity() {
      assertThat(Decorators.pipeline().process("unchanged")).isEqualTo("unchanged");
    }
  }

  @Nested
  @DisplayName("Proxy (dynamic logging proxy)")
  class ProxyPattern {

    @Test
    @DisplayName("proxy delegates to the real service and returns its result")
    void proxyDelegates() {
      List<String> log = new ArrayList<>();
      DataService proxy = StructuralPatterns.loggingProxy(new RealDataService(), log);

      assertThat(proxy.fetchData("users")).isEqualTo("data-for-users");
    }

    @Test
    @DisplayName("proxy records call and return log entries")
    void proxyLogsCalls() {
      List<String> log = new ArrayList<>();
      DataService proxy = StructuralPatterns.loggingProxy(new RealDataService(), log);

      proxy.fetchData("users");
      proxy.fetchData("orders");

      assertThat(log)
          .containsExactly(
              "CALL: fetchData([users])",
              "RETURN: data-for-users",
              "CALL: fetchData([orders])",
              "RETURN: data-for-orders");
    }

    @Test
    @DisplayName("returned object is a JDK dynamic proxy, not the real class")
    void returnedObjectIsDynamicProxy() {
      DataService proxy = StructuralPatterns.loggingProxy(new RealDataService(), new ArrayList<>());

      assertThat(Proxy.isProxyClass(proxy.getClass())).isTrue();
      assertThat(proxy).isInstanceOf(DataService.class).isNotInstanceOf(RealDataService.class);
    }
  }

  @Nested
  @DisplayName("Composite (sealed file system tree)")
  class CompositePattern {

    private final FileSystemEntry tree =
        new FileSystemEntry.Directory(
            "root",
            List.of(
                new FileSystemEntry.File("a.txt", 10),
                new FileSystemEntry.Directory(
                    "sub",
                    List.of(
                        new FileSystemEntry.File("b.txt", 5),
                        new FileSystemEntry.File("c.txt", 7))),
                new FileSystemEntry.File("d.txt", 3)));

    @Test
    @DisplayName("file size is its own size")
    void fileSize() {
      assertThat(new FileSystemEntry.File("x", 42).size()).isEqualTo(42);
    }

    @Test
    @DisplayName("directory size is the recursive total of its children")
    void directorySizeIsRecursive() {
      assertThat(tree.size()).isEqualTo(25);
    }

    @Test
    @DisplayName("empty directory has size zero")
    void emptyDirectorySize() {
      assertThat(new FileSystemEntry.Directory("empty", List.of()).size()).isZero();
    }

    @Test
    @DisplayName("listAll walks the tree producing path-like names")
    void listAllWalksTree() {
      assertThat(FileSystemEntry.listAll(tree))
          .containsExactly(
              "root/", "root/a.txt", "root/sub/", "root/sub/b.txt", "root/sub/c.txt", "root/d.txt");
    }

    @Test
    @DisplayName("listAll of a single file is just its name")
    void listAllOfFile() {
      assertThat(FileSystemEntry.listAll(new FileSystemEntry.File("only.txt", 1)))
          .containsExactly("only.txt");
    }

    @Test
    @DisplayName("listAll result for a directory is unmodifiable")
    void listAllIsUnmodifiable() {
      List<String> names = FileSystemEntry.listAll(tree);

      assertThatThrownBy(() -> names.add("extra"))
          .isInstanceOf(UnsupportedOperationException.class);
    }
  }

  @Nested
  @DisplayName("Facade (order placement)")
  class FacadePattern {

    private final OrderFacade facade = new OrderFacade();

    @Test
    @DisplayName("successful order yields ids from every subsystem")
    void successfulOrder() {
      OrderFacade.OrderResult result = facade.placeOrder("widget", 2, 9.99);

      assertThat(result.success()).isTrue();
      assertThat(result.orderId()).startsWith("ORD-");
      assertThat(result.trackingId()).startsWith("TRK-");
    }

    @Test
    @DisplayName("zero quantity fails the inventory check")
    void zeroQuantityFails() {
      OrderFacade.OrderResult result = facade.placeOrder("widget", 0, 9.99);

      assertThat(result.success()).isFalse();
      assertThat(result.orderId()).isEmpty();
      assertThat(result.trackingId()).isEmpty();
    }

    @Test
    @DisplayName("negative quantity fails the inventory check")
    void negativeQuantityFails() {
      assertThat(facade.placeOrder("widget", -1, 9.99).success()).isFalse();
    }
  }
}

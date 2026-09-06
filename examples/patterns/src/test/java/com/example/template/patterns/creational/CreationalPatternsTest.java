package com.example.template.patterns.creational;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.withinPercentage;

import com.example.template.patterns.creational.CreationalPatterns.AppConfig;
import com.example.template.patterns.creational.CreationalPatterns.Button;
import com.example.template.patterns.creational.CreationalPatterns.HttpRequest;
import com.example.template.patterns.creational.CreationalPatterns.Notification;
import com.example.template.patterns.creational.CreationalPatterns.Shape;
import com.example.template.patterns.creational.CreationalPatterns.TextInput;
import com.example.template.patterns.creational.CreationalPatterns.WidgetFactory;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for the creational GoF pattern implementations. */
@DisplayName("Creational patterns")
class CreationalPatternsTest {

  @Nested
  @DisplayName("Builder (step-builder)")
  class BuilderPattern {

    @Test
    @DisplayName("builds the fully configured request record")
    void buildsConfiguredRequest() {
      HttpRequest request =
          HttpRequest.builder()
              .method("POST")
              .url("https://api.example.com/orders")
              .header("Content-Type", "application/json")
              .header("Authorization", "Bearer token")
              .body("{\"id\":1}")
              .build();

      assertThat(request.method()).isEqualTo("POST");
      assertThat(request.url()).isEqualTo("https://api.example.com/orders");
      // Note: Map.copyOf does not preserve the builder's insertion order.
      assertThat(request.headers())
          .hasSize(2)
          .contains(
              entry("Content-Type", "application/json"), entry("Authorization", "Bearer token"));
      assertThat(request.body()).isEqualTo("{\"id\":1}");
    }

    @Test
    @DisplayName("step order enforces method then url before optional fields")
    void stepOrderProducesMinimalRequest() {
      // The step-builder types make skipping method/url a compile error;
      // at runtime the minimal chain yields a request with only the required fields.
      HttpRequest request = HttpRequest.builder().method("GET").url("https://example.com").build();

      assertThat(request.method()).isEqualTo("GET");
      assertThat(request.url()).isEqualTo("https://example.com");
      assertThat(request.headers()).isEmpty();
      assertThat(request.body()).isNull();
    }

    @Test
    @DisplayName("built header map is immutable")
    void headersAreImmutable() {
      HttpRequest request =
          HttpRequest.builder()
              .method("GET")
              .url("https://example.com")
              .header("Accept", "text/plain")
              .build();

      assertThatThrownBy(() -> request.headers().put("X", "y"))
          .isInstanceOf(UnsupportedOperationException.class);
    }
  }

  @Nested
  @DisplayName("Factory Method (sealed Shape.of)")
  class FactoryMethodPattern {

    @Test
    @DisplayName("creates the right sealed subtype per type name")
    void createsCorrectSubtypes() {
      assertThat(Shape.of("circle", 2.0)).isInstanceOf(Shape.Circle.class);
      assertThat(Shape.of("rectangle", 2.0, 3.0)).isInstanceOf(Shape.Rectangle.class);
      assertThat(Shape.of("triangle", 4.0, 5.0)).isInstanceOf(Shape.Triangle.class);
    }

    @Test
    @DisplayName("type name is case-insensitive")
    void typeNameIsCaseInsensitive() {
      assertThat(Shape.of("CIRCLE", 1.0)).isInstanceOf(Shape.Circle.class);
      assertThat(Shape.of("Rectangle", 1.0, 1.0)).isInstanceOf(Shape.Rectangle.class);
    }

    @Test
    @DisplayName("computes areas correctly")
    void computesAreas() {
      assertThat(Shape.of("circle", 2.0).area()).isCloseTo(Math.PI * 4.0, withinPercentage(1e-9));
      assertThat(Shape.of("rectangle", 2.0, 3.0).area()).isEqualTo(6.0);
      assertThat(Shape.of("triangle", 4.0, 5.0).area()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("unknown type is rejected")
    void unknownTypeThrows() {
      assertThatThrownBy(() -> Shape.of("hexagon", 1.0))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Unknown shape: hexagon");
    }

    @Test
    @DisplayName("describe pattern-matches each subtype")
    void describeMatchesSubtype() {
      var circle = new Shape.Circle(2.0);
      var rectangle = new Shape.Rectangle(2.0, 3.0);
      var triangle = new Shape.Triangle(4.0, 5.0);

      assertThat(Shape.describe(circle))
          .isEqualTo("Circle r=%.2f area=%.2f".formatted(2.0, circle.area()));
      assertThat(Shape.describe(rectangle))
          .isEqualTo("Rect %sx%s area=%.2f".formatted(2.0, 3.0, rectangle.area()));
      assertThat(Shape.describe(triangle))
          .isEqualTo("Tri base=%.2f h=%.2f area=%.2f".formatted(4.0, 5.0, triangle.area()));
    }
  }

  @Nested
  @DisplayName("Singleton (enum AppConfig)")
  class SingletonPattern {

    @Test
    @DisplayName("INSTANCE is the one and only instance")
    void instanceIsSingleton() {
      assertThat(AppConfig.values()).containsExactly(AppConfig.INSTANCE);
      assertThat(AppConfig.valueOf("INSTANCE")).isSameAs(AppConfig.INSTANCE);
    }

    @Test
    @DisplayName("config map stores and retrieves properties")
    void configMapWorks() {
      AppConfig.INSTANCE.set("singleton.test.key", "value-1");

      assertThat(AppConfig.INSTANCE.get("singleton.test.key")).isEqualTo("value-1");
      assertThat(AppConfig.INSTANCE.get("singleton.test.missing")).isNull();
      assertThat(AppConfig.INSTANCE.getOrDefault("singleton.test.missing", "fallback"))
          .isEqualTo("fallback");
      assertThat(AppConfig.INSTANCE.getOrDefault("singleton.test.key", "fallback"))
          .isEqualTo("value-1");
    }

    @Test
    @DisplayName("state set through one reference is visible through another")
    void stateIsShared() {
      AppConfig first = AppConfig.INSTANCE;
      AppConfig second = AppConfig.valueOf("INSTANCE");

      first.set("singleton.test.shared", "shared-value");

      assertThat(second.get("singleton.test.shared")).isEqualTo("shared-value");
    }
  }

  @Nested
  @DisplayName("Prototype (record withers)")
  class PrototypePattern {

    private static final String SUBJECT = "Welcome";
    private static final String BODY = "Hello!";
    private static final String NEWS = "news";

    @Test
    @DisplayName("template creates a prototype with empty recipient and no tags")
    void templateCreatesPrototype() {
      Notification prototype = Notification.template(SUBJECT, BODY);

      assertThat(prototype.recipient()).isEmpty();
      assertThat(prototype.subject()).isEqualTo(SUBJECT);
      assertThat(prototype.body()).isEqualTo(BODY);
      assertThat(prototype.tags()).isEmpty();
    }

    @Test
    @DisplayName("withRecipient copies with modification, original untouched")
    void withRecipientCopies() {
      Notification prototype = Notification.template(SUBJECT, BODY);

      Notification copy = prototype.withRecipient("alice@example.com");

      assertThat(copy).isNotSameAs(prototype);
      assertThat(copy.recipient()).isEqualTo("alice@example.com");
      assertThat(copy.subject()).isEqualTo(SUBJECT);
      assertThat(copy.body()).isEqualTo(BODY);
      assertThat(prototype.recipient()).isEmpty();
    }

    @Test
    @DisplayName("withTag appends without mutating the original")
    void withTagAppends() {
      Notification original = new Notification("bob@example.com", "S", "B", List.of(NEWS));

      Notification tagged = original.withTag("urgent");

      assertThat(tagged.tags()).containsExactly(NEWS, "urgent");
      assertThat(original.tags()).containsExactly(NEWS);
    }

    @Test
    @DisplayName("copied tag list is immutable")
    void copiedTagsAreImmutable() {
      Notification tagged = Notification.template("S", "B").withTag("one");

      assertThatThrownBy(() -> tagged.tags().add("two"))
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("compact constructor copies caller-owned tag lists")
    void compactConstructorCopiesTags() {
      var mutableTags = new java.util.ArrayList<String>();
      mutableTags.add(NEWS);

      Notification notification = new Notification("a@b.c", "S", "B", mutableTags);
      mutableTags.add("injected");

      assertThat(notification.tags()).containsExactly(NEWS);
    }
  }

  @Nested
  @DisplayName("Abstract Factory (widget registry)")
  class AbstractFactoryPattern {

    private static final String BUTTON = "button";

    @Test
    @DisplayName("creates registered widgets that render as HTML")
    void createsRegisteredWidgets() {
      var factory = new WidgetFactory();
      factory.register(BUTTON, () -> new Button("OK"));
      factory.register("input", () -> new TextInput("Name..."));

      assertThat(factory.create(BUTTON).render()).isEqualTo("<button>OK</button>");
      assertThat(factory.create("input").render()).isEqualTo("<input placeholder=\"Name...\">");
    }

    @Test
    @DisplayName("each create call invokes the supplier for a fresh instance")
    void createReturnsFreshInstances() {
      var factory = new WidgetFactory();
      factory.register(BUTTON, () -> new Button("OK"));

      assertThat(factory.create(BUTTON)).isNotSameAs(factory.create(BUTTON));
    }

    @Test
    @DisplayName("rejects unknown widget keys")
    void rejectsUnknownKeys() {
      var factory = new WidgetFactory();

      assertThatThrownBy(() -> factory.create("slider"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Unknown widget: slider");
    }
  }
}

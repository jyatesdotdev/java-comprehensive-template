package com.example.template.patterns.behavioral;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.within;

import com.example.template.patterns.behavioral.BehavioralPatterns.Command;
import com.example.template.patterns.behavioral.BehavioralPatterns.CommandHistory;
import com.example.template.patterns.behavioral.BehavioralPatterns.CsvExporter;
import com.example.template.patterns.behavioral.BehavioralPatterns.DataExporter;
import com.example.template.patterns.behavioral.BehavioralPatterns.EventBus;
import com.example.template.patterns.behavioral.BehavioralPatterns.OrderEvent;
import com.example.template.patterns.behavioral.BehavioralPatterns.PricingStrategy;
import com.example.template.patterns.behavioral.BehavioralPatterns.StringValidators;
import com.example.template.patterns.behavioral.BehavioralPatterns.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for the behavioral GoF pattern implementations. */
@DisplayName("Behavioral patterns")
class BehavioralPatternsTest {

  @Nested
  @DisplayName("Strategy (pricing strategies)")
  class StrategyPattern {

    @Test
    @DisplayName("REGULAR is price times quantity")
    void regularPricing() {
      assertThat(PricingStrategy.REGULAR.calculate(10.0, 3)).isEqualTo(30.0);
    }

    @Test
    @DisplayName("BULK discounts 10% only from 10 items up")
    void bulkPricing() {
      assertThat(PricingStrategy.BULK.calculate(10.0, 9)).isEqualTo(90.0);
      assertThat(PricingStrategy.BULK.calculate(10.0, 10)).isCloseTo(90.0, within(1e-9));
      assertThat(PricingStrategy.BULK.calculate(10.0, 20)).isCloseTo(180.0, within(1e-9));
    }

    @Test
    @DisplayName("VIP always discounts 20%")
    void vipPricing() {
      assertThat(PricingStrategy.VIP.calculate(10.0, 5)).isCloseTo(40.0, within(1e-9));
    }

    @Test
    @DisplayName("withSurcharge composes on top of the base strategy")
    void withSurchargeComposes() {
      PricingStrategy regularPlusTax = PricingStrategy.REGULAR.withSurcharge(0.10);
      PricingStrategy vipPlusFee = PricingStrategy.VIP.withSurcharge(0.05);

      assertThat(regularPlusTax.calculate(10.0, 2)).isCloseTo(22.0, within(1e-9));
      assertThat(vipPlusFee.calculate(10.0, 5)).isCloseTo(42.0, within(1e-9));
    }

    @Test
    @DisplayName("surcharge does not mutate the original strategy")
    void surchargeLeavesOriginalIntact() {
      PricingStrategy.REGULAR.withSurcharge(0.50);

      assertThat(PricingStrategy.REGULAR.calculate(10.0, 2)).isEqualTo(20.0);
    }
  }

  @Nested
  @DisplayName("Observer (EventBus with sealed OrderEvent)")
  class ObserverPattern {

    private static final String ORDER_ID = "ORD-1";

    @Test
    @DisplayName("publish delivers the event to every subscriber")
    void publishDeliversToAllSubscribers() {
      var bus = new EventBus<OrderEvent>();
      List<OrderEvent> first = new ArrayList<>();
      List<OrderEvent> second = new ArrayList<>();
      bus.subscribe(first::add);
      bus.subscribe(second::add);

      var created = new OrderEvent.Created(ORDER_ID, 99.50);
      bus.publish(created);

      assertThat(first).containsExactly(created);
      assertThat(second).containsExactly(created);
    }

    @Test
    @DisplayName("unsubscribed listener no longer receives events")
    void unsubscribeStopsDelivery() {
      var bus = new EventBus<OrderEvent>();
      List<OrderEvent> received = new ArrayList<>();
      Consumer<OrderEvent> listener = received::add;
      bus.subscribe(listener);

      bus.publish(new OrderEvent.Created(ORDER_ID, 10.0));
      bus.unsubscribe(listener);
      bus.publish(new OrderEvent.Shipped(ORDER_ID, "TRK-9"));

      assertThat(received).hasSize(1);
      assertThat(received.getFirst()).isInstanceOf(OrderEvent.Created.class);
    }

    @Test
    @DisplayName("publishing with no subscribers is a no-op")
    void publishWithoutSubscribers() {
      var bus = new EventBus<OrderEvent>();

      assertThatNoException()
          .isThrownBy(() -> bus.publish(new OrderEvent.Cancelled("ORD-2", "out of stock")));
    }

    @Test
    @DisplayName("sealed event subtypes carry their payload and shared orderId")
    void sealedEventSubtypes() {
      OrderEvent created = new OrderEvent.Created(ORDER_ID, 42.0);
      OrderEvent shipped = new OrderEvent.Shipped("ORD-2", "TRK-7");
      OrderEvent cancelled = new OrderEvent.Cancelled("ORD-3", "fraud");

      assertThat(created.orderId()).isEqualTo(ORDER_ID);
      assertThat(shipped.orderId()).isEqualTo("ORD-2");
      assertThat(cancelled.orderId()).isEqualTo("ORD-3");

      String description =
          switch (created) {
            case OrderEvent.Created c -> "created total=" + c.total();
            case OrderEvent.Shipped s -> "shipped " + s.trackingId();
            case OrderEvent.Cancelled c -> "cancelled " + c.reason();
          };
      assertThat(description).isEqualTo("created total=42.0");
    }
  }

  @Nested
  @DisplayName("Command (undoable list operations)")
  class CommandPattern {

    private static final String BOOK = "book";
    private static final String PEN = "pen";

    @Test
    @DisplayName("AddItem executes and undoes symmetrically")
    void addItemExecutesAndUndoes() {
      List<String> cart = new ArrayList<>();
      var add = new Command.AddItem<>(cart, BOOK);

      assertThat(add.execute()).isTrue();
      assertThat(cart).containsExactly(BOOK);

      add.undo();
      assertThat(cart).isEmpty();
    }

    @Test
    @DisplayName("RemoveItem executes and undoes symmetrically")
    void removeItemExecutesAndUndoes() {
      List<String> cart = new ArrayList<>(List.of(BOOK, PEN));
      var remove = new Command.RemoveItem<>(cart, BOOK);

      assertThat(remove.execute()).isTrue();
      assertThat(cart).containsExactly(PEN);

      remove.undo();
      assertThat(cart).containsExactlyInAnyOrder(PEN, BOOK);
    }

    @Test
    @DisplayName("history executes, tracks, and undoes in LIFO order")
    void historyUndoesInLifoOrder() {
      List<String> cart = new ArrayList<>();
      var history = new CommandHistory();

      assertThat(history.<Boolean>execute(new Command.AddItem<>(cart, BOOK))).isTrue();
      assertThat(history.<Boolean>execute(new Command.AddItem<>(cart, PEN))).isTrue();
      assertThat(history.size()).isEqualTo(2);
      assertThat(cart).containsExactly(BOOK, PEN);

      history.undo();
      assertThat(cart).containsExactly(BOOK);
      assertThat(history.size()).isEqualTo(1);

      history.undo();
      assertThat(cart).isEmpty();
      assertThat(history.size()).isZero();
    }

    @Test
    @DisplayName("undo on empty history is a safe no-op")
    void undoOnEmptyHistory() {
      var history = new CommandHistory();

      assertThatNoException().isThrownBy(history::undo);
      assertThat(history.size()).isZero();
    }
  }

  @Nested
  @DisplayName("Template Method (data exporters)")
  class TemplateMethodPattern {

    @Test
    @DisplayName("CSV exporter emits header then rows")
    void csvExporterOutput() {
      var exporter = new CsvExporter("id", "name");

      String csv = exporter.export(List.of(new String[] {"1", "Alice"}, new String[] {"2", "Bob"}));

      assertThat(csv).isEqualTo("id,name\n1,Alice\n2,Bob\n");
    }

    @Test
    @DisplayName("CSV exporter with no rows emits only the header")
    void csvExporterEmpty() {
      assertThat(new CsvExporter("id").export(List.of())).isEqualTo("id\n");
    }

    @Test
    @DisplayName("footer hook is empty by default and overridable")
    void footerHookIsOverridable() {
      DataExporter<String> withFooter =
          new DataExporter<>() {
            @Override
            protected String header() {
              return "<list>";
            }

            @Override
            protected String formatItem(String item) {
              return "<item>" + item + "</item>";
            }

            @Override
            protected String footer() {
              return "</list>";
            }
          };

      assertThat(withFooter.export(List.of("a", "b")))
          .isEqualTo("<list><item>a</item><item>b</item></list>");
    }
  }

  @Nested
  @DisplayName("Chain of Responsibility (validators)")
  class ChainOfResponsibilityPattern {

    private static final String BLANK_ERROR = "must not be blank";

    @Test
    @DisplayName("Validator.of returns no errors when the predicate passes")
    void validatorOfPasses() {
      Validator<String> nonEmpty = Validator.of(s -> !s.isEmpty(), "must not be empty");

      assertThat(nonEmpty.validate("x")).isEmpty();
    }

    @Test
    @DisplayName("Validator.of returns the error message when the predicate fails")
    void validatorOfFails() {
      Validator<String> nonEmpty = Validator.of(s -> !s.isEmpty(), "must not be empty");

      assertThat(nonEmpty.validate("")).containsExactly("must not be empty");
    }

    @Test
    @DisplayName("andThen runs every validator and merges errors in chain order")
    void andThenMergesAllErrors() {
      Validator<String> chain =
          StringValidators.notBlank()
              .andThen(StringValidators.maxLength(3))
              .andThen(StringValidators.matches("[a-z]+"));

      // Documented behavior: the chain does NOT short-circuit — all links run.
      assertThat(chain.validate(null))
          .containsExactly(BLANK_ERROR, "must be at most 3 chars", "must match [a-z]+");
      assertThat(chain.validate("ABCDE"))
          .containsExactly("must be at most 3 chars", "must match [a-z]+");
      assertThat(chain.validate("abc")).isEmpty();
    }

    @Test
    @DisplayName("notBlank rejects null, empty, and whitespace-only strings")
    void notBlankRule() {
      Validator<String> validator = StringValidators.notBlank();

      assertThat(validator.validate("ok")).isEmpty();
      assertThat(validator.validate(null)).containsExactly(BLANK_ERROR);
      assertThat(validator.validate("")).containsExactly(BLANK_ERROR);
      assertThat(validator.validate("   ")).containsExactly(BLANK_ERROR);
    }

    @Test
    @DisplayName("maxLength accepts up to the limit and rejects beyond")
    void maxLengthRule() {
      Validator<String> validator = StringValidators.maxLength(5);

      assertThat(validator.validate("12345")).isEmpty();
      assertThat(validator.validate("123456")).containsExactly("must be at most 5 chars");
      assertThat(validator.validate(null)).containsExactly("must be at most 5 chars");
    }

    @Test
    @DisplayName("matches applies the regex to the whole string")
    void matchesRule() {
      Validator<String> validator = StringValidators.matches("\\d+");

      assertThat(validator.validate("12345")).isEmpty();
      assertThat(validator.validate("12a45")).containsExactly("must match \\d+");
      assertThat(validator.validate(null)).containsExactly("must match \\d+");
    }
  }
}

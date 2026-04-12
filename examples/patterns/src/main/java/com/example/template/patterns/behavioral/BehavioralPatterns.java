package com.example.template.patterns.behavioral;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Behavioral GoF patterns using modern Java (17+).
 *
 * <ul>
 *   <li><b>Strategy</b> — functional interfaces as strategies</li>
 *   <li><b>Observer</b> — type-safe event bus</li>
 *   <li><b>Command</b> — undoable command with sealed interface</li>
 *   <li><b>Template Method</b> — abstract class with hook methods</li>
 *   <li><b>Chain of Responsibility</b> — composable handler pipeline</li>
 *   <li><b>Iterator</b> — custom Iterable over a tree</li>
 * </ul>
 */
public final class BehavioralPatterns {
    private BehavioralPatterns() {}

    // ── Strategy: pricing strategies as functional interfaces ──

    /** Strategy for computing order totals. Implementations are composable via {@link #withSurcharge(double)}. */
    @FunctionalInterface
    public interface PricingStrategy {
        /**
         * Calculates the total price.
         *
         * @param basePrice unit price
         * @param quantity  number of items
         * @return computed total
         */
        double calculate(double basePrice, int quantity);

        /** Standard pricing: {@code price × qty}. */
        PricingStrategy REGULAR = (price, qty) -> price * qty;
        /** Bulk pricing: 10% discount when quantity ≥ 10. */
        PricingStrategy BULK    = (price, qty) -> price * qty * (qty >= 10 ? 0.9 : 1.0);
        /** VIP pricing: flat 20% discount. */
        PricingStrategy VIP     = (price, qty) -> price * qty * 0.8;

        /**
         * Composes this strategy with a percentage surcharge applied after calculation.
         *
         * @param pct surcharge as a decimal (e.g. 0.05 for 5%)
         * @return a new strategy that adds the surcharge
         */
        default PricingStrategy withSurcharge(double pct) {
            return (price, qty) -> this.calculate(price, qty) * (1 + pct);
        }
    }

    // ── Observer: type-safe event bus ──

    /**
     * Type-safe event bus using the Observer pattern.
     *
     * @param <E> event type
     */
    public static final class EventBus<E> {
        private final List<Consumer<E>> listeners = new CopyOnWriteArrayList<>();

        /**
         * Subscribes a listener to receive events.
         *
         * @param listener event consumer
         */
        public void subscribe(Consumer<E> listener) { listeners.add(listener); }

        /**
         * Unsubscribes a previously registered listener.
         *
         * @param listener event consumer to remove
         */
        public void unsubscribe(Consumer<E> listener) { listeners.remove(listener); }

        /**
         * Publishes an event to all subscribers.
         *
         * @param event the event to broadcast
         */
        public void publish(E event) { listeners.forEach(l -> l.accept(event)); }
    }

    /** Sealed hierarchy of order domain events. */
    public sealed interface OrderEvent {
        /**
         * Returns the order identifier.
         *
         * @return order ID
         */
        String orderId();

        /**
         * An order was created.
         *
         * @param orderId order identifier
         * @param total   order total
         */
        record Created(String orderId, double total) implements OrderEvent {}

        /**
         * An order was shipped.
         *
         * @param orderId    order identifier
         * @param trackingId shipment tracking ID
         */
        record Shipped(String orderId, String trackingId) implements OrderEvent {}

        /**
         * An order was cancelled.
         *
         * @param orderId order identifier
         * @param reason  cancellation reason
         */
        record Cancelled(String orderId, String reason) implements OrderEvent {}
    }

    // ── Command: undoable operations with sealed interface ──

    /**
     * Sealed command interface supporting execute and undo.
     *
     * @param <T> the result type of {@link #execute()}
     */
    public sealed interface Command<T> {
        /**
         * Executes the command.
         *
         * @return the result
         */
        T execute();

        /** Reverses the effect of {@link #execute()}. */
        void undo();

        /**
         * Command that adds an item to a list.
         *
         * @param <T>  element type
         * @param list target list
         * @param item item to add
         */
        record AddItem<T>(List<T> list, T item) implements Command<Boolean> {
            public Boolean execute() { return list.add(item); }
            public void undo() { list.remove(item); }
        }

        /**
         * Command that removes an item from a list.
         *
         * @param <T>  element type
         * @param list target list
         * @param item item to remove
         */
        record RemoveItem<T>(List<T> list, T item) implements Command<Boolean> {
            public Boolean execute() { return list.remove(item); }
            public void undo() { list.add(item); }
        }
    }

    /** Maintains a history stack of executed commands for undo support. */
    public static final class CommandHistory {
        private final Deque<Command<?>> history = new LinkedList<>();

        /**
         * Executes a command and pushes it onto the history stack.
         *
         * @param <T> result type
         * @param cmd command to execute
         * @return the command's result
         */
        public <T> T execute(Command<T> cmd) {
            T result = cmd.execute();
            history.push(cmd);
            return result;
        }

        /** Undoes the most recent command, if any. */
        public void undo() {
            if (!history.isEmpty()) history.pop().undo();
        }

        /**
         * Returns the number of commands in the history.
         *
         * @return history size
         */
        public int size() { return history.size(); }
    }

    // ── Template Method: data export framework ──

    /**
     * Template Method base class for exporting a list of items to a string format.
     *
     * @param <T> item type
     */
    public abstract static class DataExporter<T> {
        /**
         * Template method — defines the export algorithm skeleton: header → items → footer.
         *
         * @param items items to export
         * @return the formatted output string
         */
        public final String export(List<T> items) {
            var sb = new StringBuilder();
            sb.append(header());
            for (T item : items) sb.append(formatItem(item));
            sb.append(footer());
            return sb.toString();
        }

        /**
         * Returns the header string for the output.
         *
         * @return header content
         */
        protected abstract String header();

        /**
         * Formats a single item.
         *
         * @param item the item to format
         * @return formatted string for this item
         */
        protected abstract String formatItem(T item);

        /**
         * Returns the footer string. Default is empty — override to customize.
         *
         * @return footer content
         */
        protected String footer() { return ""; } // hook — optional override
    }

    /**
     * CSV exporter — concrete {@link DataExporter} for string-array rows.
     */
    public static final class CsvExporter extends DataExporter<String[]> {
        private final String[] columns;

        /**
         * Creates a CSV exporter with the given column headers.
         *
         * @param columns column header names
         */
        public CsvExporter(String... columns) { this.columns = columns; }

        @Override protected String header() { return String.join(",", columns) + "\n"; }
        @Override protected String formatItem(String[] row) { return String.join(",", row) + "\n"; }
    }

    // ── Chain of Responsibility: composable validation pipeline ──

    /**
     * Chain of Responsibility: composable validation pipeline.
     *
     * @param <T> type being validated
     */
    @FunctionalInterface
    public interface Validator<T> {
        /**
         * Validates the input.
         *
         * @param input value to validate
         * @return list of error messages (empty if valid)
         */
        List<String> validate(T input);

        /**
         * Chains this validator with another; both are evaluated and errors are merged.
         *
         * @param next the next validator in the chain
         * @return a combined validator
         */
        default Validator<T> andThen(Validator<T> next) {
            return input -> {
                var errors = new ArrayList<>(this.validate(input));
                errors.addAll(next.validate(input));
                return errors;
            };
        }

        /**
         * Creates a validator from a predicate and an error message.
         *
         * @param <T>      type being validated
         * @param check    predicate that returns {@code true} when valid
         * @param errorMsg error message when the check fails
         * @return a new validator
         */
        static <T> Validator<T> of(Predicate<T> check, String errorMsg) {
            return input -> check.test(input) ? List.of() : List.of(errorMsg);
        }
    }

    /** Pre-built string validators for common constraints. */
    public static final class StringValidators {
        /**
         * Validates that a string is not null or blank.
         *
         * @return a not-blank validator
         */
        public static Validator<String> notBlank() {
            return Validator.of(s -> s != null && !s.isBlank(), "must not be blank");
        }
        /**
         * Validates that a string does not exceed the given length.
         *
         * @param max maximum allowed length
         * @return a max-length validator
         */
        public static Validator<String> maxLength(int max) {
            return Validator.of(s -> s != null && s.length() <= max, "must be at most " + max + " chars");
        }

        /**
         * Validates that a string matches the given regex.
         *
         * @param regex regular expression pattern
         * @return a regex-matching validator
         */
        public static Validator<String> matches(String regex) {
            return Validator.of(s -> s != null && s.matches(regex), "must match " + regex);
        }
    }
}

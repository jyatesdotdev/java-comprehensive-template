package com.example.template.patterns.structural;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Structural GoF patterns using modern Java (17+).
 *
 * <ul>
 *   <li><b>Adapter</b> — adapting legacy API via functional interface</li>
 *   <li><b>Decorator</b> — composable decorators via {@link UnaryOperator}</li>
 *   <li><b>Proxy</b> — dynamic proxy for logging/caching</li>
 *   <li><b>Composite</b> — sealed interface tree</li>
 *   <li><b>Facade</b> — simplified API over complex subsystem</li>
 * </ul>
 */
public final class StructuralPatterns {
    private StructuralPatterns() {}

    // ── Adapter: wrap a legacy interface behind a modern one ──

    /**
     * Legacy third-party printer API we cannot change.
     */
    public static final class LegacyPrinter {
        /**
         * Prints text left-padded to the given width.
         *
         * @param text  text to print
         * @param width field width
         * @return formatted string
         */
        public String printFormatted(String text, int width) {
            return "%-{}s".replace("{}", String.valueOf(width)).formatted(text);
        }
    }

    /** Modern printer interface our code expects. */
    @FunctionalInterface
    public interface Printer {
        /**
         * Prints the given text.
         *
         * @param text text to print
         * @return formatted output
         */
        String print(String text);
    }

    /**
     * Adapts a {@link LegacyPrinter} to the {@link Printer} interface.
     *
     * @param legacy the legacy printer to wrap
     * @param width  field width to use
     * @return a {@link Printer} backed by the legacy implementation
     */
    public static Printer adapt(LegacyPrinter legacy, int width) {
        return text -> legacy.printFormatted(text, width);
    }

    // ── Decorator: composable via UnaryOperator chaining ──

    /** Composable text processor demonstrating the Decorator pattern via functional chaining. */
    @FunctionalInterface
    public interface TextProcessor {
        /**
         * Processes the input text.
         *
         * @param input text to process
         * @return processed text
         */
        String process(String input);

        /**
         * Chains this processor with another, applying {@code next} after this one.
         *
         * @param next the processor to apply after this one
         * @return a composed processor
         */
        default TextProcessor andThen(TextProcessor next) {
            return input -> next.process(this.process(input));
        }
    }

    /** Library of reusable {@link TextProcessor} decorators. */
    public static final class Decorators {
        /** @return a processor that trims whitespace */
        public static TextProcessor trimming()    { return String::trim; }
        /** @return a processor that converts to upper case */
        public static TextProcessor upperCase()   { return String::toUpperCase; }

        /**
         * @param p prefix string
         * @return a processor that prepends the prefix
         */
        public static TextProcessor prefix(String p) { return s -> p + s; }

        /**
         * @param s suffix string
         * @return a processor that appends the suffix
         */
        public static TextProcessor suffix(String s) { return input -> input + s; }

        /**
         * Composes multiple decorators into a single pipeline.
         *
         * @param processors processors to chain in order
         * @return a composed processor
         */
        public static TextProcessor pipeline(TextProcessor... processors) {
            TextProcessor result = s -> s; // identity
            for (var p : processors) result = result.andThen(p);
            return result;
        }
    }

    // ── Proxy: dynamic proxy for logging ──

    /** Service interface for fetching data by key. */
    public interface DataService {
        /**
         * Fetches data for the given key.
         *
         * @param key lookup key
         * @return the data string
         */
        String fetchData(String key);
    }

    /** Real implementation of {@link DataService}. */
    public static final class RealDataService implements DataService {
        @Override public String fetchData(String key) { return "data-for-" + key; }
    }

    /**
     * Creates a dynamic proxy that logs all method calls on a {@link DataService}.
     *
     * @param target the real service to delegate to
     * @param log    mutable list that receives log entries
     * @return a logging proxy wrapping the target
     */
    @SuppressWarnings("unchecked")
    public static DataService loggingProxy(DataService target, List<String> log) {
        InvocationHandler handler = (proxy, method, args) -> {
            log.add("CALL: %s(%s)".formatted(method.getName(), args != null ? List.of(args) : "[]"));
            Object result = method.invoke(target, args);
            log.add("RETURN: " + result);
            return result;
        };
        return (DataService) Proxy.newProxyInstance(
                DataService.class.getClassLoader(),
                new Class[]{DataService.class},
                handler);
    }

    // ── Composite: sealed interface tree ──

    /** Sealed composite tree representing a file system hierarchy. */
    public sealed interface FileSystemEntry permits FileSystemEntry.File, FileSystemEntry.Directory {
        /**
         * Returns the entry name.
         *
         * @return name
         */
        String name();

        /**
         * Returns the size in bytes (files) or the recursive total (directories).
         *
         * @return size in bytes
         */
        long size();

        /**
         * A leaf file entry.
         *
         * @param name file name
         * @param size file size in bytes
         */
        record File(String name, long size) implements FileSystemEntry {}

        /**
         * A directory containing child entries.
         *
         * @param name     directory name
         * @param children child entries
         */
        record Directory(String name, List<FileSystemEntry> children) implements FileSystemEntry {
            public long size() {
                return children.stream().mapToLong(FileSystemEntry::size).sum();
            }
        }

        /**
         * Recursively lists all file and directory names in the tree.
         *
         * @param entry root entry to traverse
         * @return list of path-like names
         */
        static List<String> listAll(FileSystemEntry entry) {
            return switch (entry) {
                case File f -> List.of(f.name());
                case Directory d -> {
                    var result = new ArrayList<String>();
                    result.add(d.name() + "/");
                    d.children().forEach(c -> listAll(c).forEach(n -> result.add(d.name() + "/" + n)));
                    yield Collections.unmodifiableList(result);
                }
            };
        }
    }

    // ── Facade: simplified API over complex subsystem ──

    /**
     * Inventory item in the order subsystem.
     *
     * @param item item name
     * @param qty  quantity in stock
     */
    public record Inventory(String item, int qty) {}

    /**
     * Payment result in the order subsystem.
     *
     * @param orderId order identifier
     * @param amount  payment amount
     * @param success whether the payment succeeded
     */
    public record Payment(String orderId, double amount, boolean success) {}

    /**
     * Shipment record in the order subsystem.
     *
     * @param orderId    order identifier
     * @param trackingId shipment tracking ID
     */
    public record Shipment(String orderId, String trackingId) {}

    /** Facade that hides inventory, payment, and shipping complexity behind a single method. */
    public static final class OrderFacade {
        /**
         * Result of placing an order.
         *
         * @param orderId    order identifier
         * @param trackingId shipment tracking ID (empty on failure)
         * @param success    whether the order was placed successfully
         */
        public record OrderResult(String orderId, String trackingId, boolean success) {}

        /**
         * Places an order: checks inventory, processes payment, and ships.
         *
         * @param item  item name
         * @param qty   quantity to order
         * @param price unit price
         * @return the order result
         */
        public OrderResult placeOrder(String item, int qty, double price) {
            // 1. Check inventory
            var inv = new Inventory(item, qty);
            if (inv.qty() <= 0) return new OrderResult("", "", false);
            // 2. Process payment
            String orderId = "ORD-" + System.nanoTime();
            var payment = new Payment(orderId, qty * price, true);
            if (!payment.success()) return new OrderResult(orderId, "", false);
            // 3. Ship
            var shipment = new Shipment(orderId, "TRK-" + orderId.hashCode());
            return new OrderResult(orderId, shipment.trackingId(), true);
        }
    }
}

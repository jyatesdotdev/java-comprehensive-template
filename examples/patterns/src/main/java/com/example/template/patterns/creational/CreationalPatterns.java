package com.example.template.patterns.creational;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Creational GoF patterns using modern Java (17+).
 *
 * <ul>
 *   <li><b>Builder</b> — step builder via sealed interfaces</li>
 *   <li><b>Factory Method</b> — sealed interface + pattern matching</li>
 *   <li><b>Singleton</b> — enum-based</li>
 *   <li><b>Prototype</b> — record-based deep copy</li>
 *   <li><b>Abstract Factory</b> — functional factory registry</li>
 * </ul>
 */
public final class CreationalPatterns {
    private CreationalPatterns() {}

    // ── Builder: step-builder enforcing required fields at compile time ──

    /**
     * Immutable HTTP request built via a step-builder that enforces required fields at compile time.
     *
     * @param method  HTTP method (GET, POST, etc.)
     * @param url     target URL
     * @param headers unmodifiable header map
     * @param body    optional request body
     */
    public record HttpRequest(String method, String url, Map<String, String> headers, String body) {

        /**
         * Starts the step-builder chain.
         *
         * @return the first builder step requiring an HTTP method
         */
        public static MethodStep builder() { return method -> url -> new Builder(method, url); }

        /** Step requiring the HTTP method. */
        public interface MethodStep {
            /**
             * Sets the HTTP method.
             *
             * @param method HTTP method string
             * @return the next step
             */
            UrlStep method(String method);
        }

        /** Step requiring the target URL. */
        public interface UrlStep {
            /**
             * Sets the target URL.
             *
             * @param url target URL
             * @return a mutable builder for optional fields
             */
            Builder url(String url);
        }

        /** Mutable builder for optional HTTP request fields (headers, body). */
        public static final class Builder {
            private final String method;
            private final String url;
            private final Map<String, String> headers = new java.util.LinkedHashMap<>();
            private String body;

            private Builder(String method, String url) {
                this.method = method;
                this.url = url;
            }

            /**
             * Adds a header.
             *
             * @param key   header name
             * @param value header value
             * @return this builder
             */
            public Builder header(String key, String value) { headers.put(key, value); return this; }

            /**
             * Sets the request body.
             *
             * @param body request body
             * @return this builder
             */
            public Builder body(String body) { this.body = body; return this; }

            /**
             * Builds an immutable {@link HttpRequest}.
             *
             * @return the constructed request
             */
            public HttpRequest build() { return new HttpRequest(method, url, Map.copyOf(headers), body); }
        }
    }

    // ── Factory Method: sealed interface + static factory with pattern matching ──

    /**
     * Sealed shape hierarchy demonstrating the Factory Method pattern with pattern matching.
     */
    public sealed interface Shape permits Shape.Circle, Shape.Rectangle, Shape.Triangle {
        /**
         * Computes the area of this shape.
         *
         * @return area in square units
         */
        double area();

        /**
         * Circle defined by its radius.
         *
         * @param radius circle radius
         */
        record Circle(double radius) implements Shape {
            public double area() { return Math.PI * radius * radius; }
        }

        /**
         * Rectangle defined by width and height.
         *
         * @param width  rectangle width
         * @param height rectangle height
         */
        record Rectangle(double width, double height) implements Shape {
            public double area() { return width * height; }
        }

        /**
         * Triangle defined by base and height.
         *
         * @param base   triangle base
         * @param height triangle height
         */
        record Triangle(double base, double height) implements Shape {
            public double area() { return 0.5 * base * height; }
        }

        /**
         * Factory method — returns the right subtype based on input.
         *
         * @param type shape type name ({@code "circle"}, {@code "rectangle"}, or {@code "triangle"})
         * @param dims dimensions (radius for circle; width+height for rectangle/triangle)
         * @return the constructed {@link Shape}
         * @throws IllegalArgumentException if the type is unknown
         */
        static Shape of(String type, double... dims) {
            return switch (type.toLowerCase()) {
                case "circle"    -> new Circle(dims[0]);
                case "rectangle" -> new Rectangle(dims[0], dims[1]);
                case "triangle"  -> new Triangle(dims[0], dims[1]);
                default -> throw new IllegalArgumentException("Unknown shape: " + type);
            };
        }

        /**
         * Pattern-matching description (Java 21+ preview, shown for reference).
         *
         * @param s shape to describe
         * @return human-readable description including dimensions and area
         */
        static String describe(Shape s) {
            return switch (s) {
                case Circle c    -> "Circle r=%.2f area=%.2f".formatted(c.radius(), c.area());
                case Rectangle r -> "Rect %sx%s area=%.2f".formatted(r.width(), r.height(), r.area());
                case Triangle t  -> "Tri base=%.2f h=%.2f area=%.2f".formatted(t.base(), t.height(), t.area());
            };
        }
    }

    // ── Singleton: enum-based (thread-safe, serialization-safe) ──

    /**
     * Enum-based singleton providing thread-safe, serialization-safe application configuration.
     */
    public enum AppConfig {
        /** The single instance. */
        INSTANCE;

        private final Map<String, String> properties = new ConcurrentHashMap<>();

        /**
         * Sets a configuration property.
         *
         * @param key   property key
         * @param value property value
         */
        public void set(String key, String value) { properties.put(key, value); }

        /**
         * Gets a configuration property.
         *
         * @param key property key
         * @return the value, or {@code null} if absent
         */
        public String get(String key) { return properties.get(key); }

        /**
         * Gets a configuration property with a default fallback.
         *
         * @param key property key
         * @param def default value if key is absent
         * @return the value, or {@code def} if absent
         */
        public String getOrDefault(String key, String def) { return properties.getOrDefault(key, def); }
    }

    // ── Prototype: record-based with copy-and-modify via wither methods ──

    /**
     * Immutable notification demonstrating the Prototype pattern via record wither methods.
     *
     * @param recipient message recipient
     * @param subject   message subject
     * @param body      message body
     * @param tags      immutable list of tags
     */
    public record Notification(String recipient, String subject, String body, List<String> tags) {

        /**
         * Immutable copy with a different recipient.
         *
         * @param recipient new recipient
         * @return a new {@link Notification} with the given recipient
         */
        public Notification withRecipient(String recipient) {
            return new Notification(recipient, subject, body, tags);
        }

        /**
         * Immutable copy with an extra tag appended.
         *
         * @param tag tag to add
         * @return a new {@link Notification} with the tag appended
         */
        public Notification withTag(String tag) {
            var newTags = new java.util.ArrayList<>(tags);
            newTags.add(tag);
            return new Notification(recipient, subject, body, List.copyOf(newTags));
        }

        /**
         * Creates a template prototype with an empty recipient and no tags.
         *
         * @param subject message subject
         * @param body    message body
         * @return a prototype {@link Notification}
         */
        public static Notification template(String subject, String body) {
            return new Notification("", subject, body, List.of());
        }
    }

    // ── Abstract Factory: functional registry ──

    /** Renderable UI widget (Abstract Factory product interface). */
    public interface Widget {
        /**
         * Renders this widget as an HTML string.
         *
         * @return HTML representation
         */
        String render();
    }

    /**
     * A button widget.
     *
     * @param label button label text
     */
    public record Button(String label) implements Widget { public String render() { return "<button>" + label + "</button>"; } }

    /**
     * A text input widget.
     *
     * @param placeholder placeholder text
     */
    public record TextInput(String placeholder) implements Widget { public String render() { return "<input placeholder=\"" + placeholder + "\">"; } }

    /** Functional registry-based Abstract Factory for {@link Widget} instances. */
    public static final class WidgetFactory {
        private final Map<String, Supplier<Widget>> registry = new ConcurrentHashMap<>();

        /**
         * Registers a widget creator under the given name.
         *
         * @param name    widget type name
         * @param creator supplier that creates the widget
         */
        public void register(String name, Supplier<Widget> creator) { registry.put(name, creator); }

        /**
         * Creates a widget by name.
         *
         * @param name registered widget type name
         * @return the created widget
         * @throws IllegalArgumentException if the name is not registered
         */
        public Widget create(String name) {
            var creator = registry.get(name);
            if (creator == null) throw new IllegalArgumentException("Unknown widget: " + name);
            return creator.get();
        }
    }
}

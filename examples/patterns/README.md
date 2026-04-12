# Design Patterns — Modern Java (17+)

GoF design patterns implemented with modern Java idioms: records, sealed interfaces, functional interfaces, pattern matching, and lambdas.

## Contents

### Creational Patterns (`creational/CreationalPatterns.java`)

| Pattern | Modern Java Approach | Key Benefit |
|---------|---------------------|-------------|
| **Builder** | Step-builder with sealed interfaces enforcing required fields at compile time | Type-safe construction — can't forget required params |
| **Factory Method** | Sealed interface + `switch` expression | Exhaustive matching, no `instanceof` chains |
| **Singleton** | Enum-based (`AppConfig.INSTANCE`) | Thread-safe, serialization-safe, zero boilerplate |
| **Prototype** | Record with `with*()` copy methods | Immutable templates — clone-and-modify safely |
| **Abstract Factory** | `Map<String, Supplier<Widget>>` registry | Register/create widgets without class hierarchies |

### Structural Patterns (`structural/StructuralPatterns.java`)

| Pattern | Modern Java Approach | Key Benefit |
|---------|---------------------|-------------|
| **Adapter** | Lambda wrapping legacy API behind `@FunctionalInterface` | One-liner adaptation, no adapter class needed |
| **Decorator** | `TextProcessor` with `andThen()` composition | Stackable pipeline: `trimming().andThen(upperCase())` |
| **Proxy** | `java.lang.reflect.Proxy` dynamic proxy | Cross-cutting concerns (logging, caching) without subclassing |
| **Composite** | Sealed interface with `File` and `Directory` records | Recursive tree with exhaustive pattern matching |
| **Facade** | `OrderFacade` hiding inventory/payment/shipping | Single entry point for complex multi-step workflows |

### Behavioral Patterns (`behavioral/BehavioralPatterns.java`)

| Pattern | Modern Java Approach | Key Benefit |
|---------|---------------------|-------------|
| **Strategy** | `@FunctionalInterface PricingStrategy` with lambda constants | Swap algorithms at runtime; compose with `withSurcharge()` |
| **Observer** | Generic `EventBus<E>` with `Consumer<E>` listeners | Type-safe pub/sub; sealed `OrderEvent` hierarchy |
| **Command** | Sealed `Command<T>` interface with `execute()`/`undo()` | Undoable operations with `CommandHistory` stack |
| **Template Method** | Abstract `DataExporter<T>` with hook methods | Algorithm skeleton in base class; concrete `CsvExporter` |
| **Chain of Responsibility** | `Validator<T>` with `andThen()` composition | Composable validation: `notBlank().andThen(maxLength(50))` |

## Modern Java Techniques Used

- **Records** — immutable value types replacing verbose POJOs (Builder result, Composite nodes, Events)
- **Sealed interfaces** — closed type hierarchies enabling exhaustive `switch` (Shape, FileSystemEntry, Command, OrderEvent)
- **Functional interfaces** — strategies, decorators, validators as lambdas
- **Pattern matching** — `switch` expressions over sealed types (Shape.describe, FileSystemEntry.listAll)
- **`andThen()` composition** — decorator and chain-of-responsibility via method chaining
- **`CopyOnWriteArrayList`** — thread-safe observer list

## Usage Examples

```java
// Builder — step builder enforces method/url before optional fields
var request = HttpRequest.builder()
    .method("POST").url("/api/orders")
    .header("Content-Type", "application/json")
    .body("{\"item\":\"widget\"}")
    .build();

// Factory Method — sealed interface with pattern matching
Shape circle = Shape.of("circle", 5.0);
System.out.println(Shape.describe(circle)); // "Circle r=5.00 area=78.54"

// Decorator — composable text pipeline
var pipeline = Decorators.pipeline(
    Decorators.trimming(),
    Decorators.upperCase(),
    Decorators.prefix("[LOG] ")
);
pipeline.process("  hello world  "); // "[LOG] HELLO WORLD"

// Strategy — swap pricing at runtime
double price = PricingStrategy.BULK.withSurcharge(0.05).calculate(10.0, 15);

// Observer — type-safe events
var bus = new EventBus<OrderEvent>();
bus.subscribe(e -> System.out.println("Event: " + e));
bus.publish(new OrderEvent.Created("ORD-1", 99.99));

// Command — undoable operations
var history = new CommandHistory();
var list = new ArrayList<>(List.of("a", "b"));
history.execute(new Command.AddItem<>(list, "c")); // list = [a, b, c]
history.undo();                                     // list = [a, b]

// Validator — composable chain
var validator = StringValidators.notBlank()
    .andThen(StringValidators.maxLength(50))
    .andThen(StringValidators.matches("[a-zA-Z ]+"));
List<String> errors = validator.validate(""); // ["must not be blank", "must match [a-zA-Z ]+"]
```

## When to Use Which Pattern

| Need | Pattern |
|------|---------|
| Complex object construction with many optional fields | Builder |
| Create objects without specifying exact class | Factory Method / Abstract Factory |
| Ensure exactly one instance | Singleton (enum) |
| Clone and customize immutable templates | Prototype (record + wither) |
| Integrate incompatible interfaces | Adapter |
| Add behavior without modifying existing code | Decorator |
| Control access or add cross-cutting concerns | Proxy |
| Represent tree/part-whole hierarchies | Composite |
| Simplify a complex subsystem | Facade |
| Swap algorithms at runtime | Strategy |
| Notify multiple listeners of state changes | Observer |
| Encapsulate and undo operations | Command |
| Define algorithm skeleton, let subclasses fill steps | Template Method |
| Pass request through a chain of handlers | Chain of Responsibility / Validator |

## How to Run

```bash
# From project root
./mvnw -pl examples/patterns compile

# Run tests
./mvnw -pl examples/patterns test
```

## Related Documentation

- [Main README](../../README.md) — Project overview and quick start
- [Architecture Patterns](../../docs/architecture-patterns.md) — Hexagonal, CQRS patterns
- [Best Practices](../../docs/best-practices.md) — Code style, records, sealed classes
- [Tutorial](../../docs/TUTORIAL.md) — New developer walkthrough

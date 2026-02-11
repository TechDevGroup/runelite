---
name: coding-conventions
description: Enforce clean code standards including 1-level nesting, early returns, single responsibility, branchless paradigms, 1D iteration, options pattern, and chain/fluent patterns. Use when writing or refactoring code to ensure optimal structure.

allowed-tools:
  - Read
  - Write
  - Edit
  - Grep
  - Glob

version: 1.0.0
---

# Clean Code Conventions Enforcer

You are a software architecture specialist focused on enforcing rigorous clean code standards. Your role is to guide, implement, and refactor code to follow strict conventions that maximize readability, maintainability, and performance.

## Core Principles

### 1. Single-Level Nesting Only
**Rule**: Maximum 1 level of indentation inside function bodies.

**Anti-pattern (Java)**:
```java
public List<Result> processData(List<Item> items) {
    List<Result> results = new ArrayList<>();
    for (Item item : items) {
        if (item.isValid()) {
            for (SubItem sub : item.getChildren()) {
                if (sub.isActive()) {
                    results.add(sub.process());
                }
            }
        }
    }
    return results;
}
```

**Correct pattern (Java)**:
```java
public List<Result> processData(List<Item> items) {
    List<Item> validItems = filterValid(items);
    List<SubItem> activeChildren = extractActiveChildren(validItems);
    return processChildren(activeChildren);
}

private List<Item> filterValid(List<Item> items) {
    return items.stream()
        .filter(Item::isValid)
        .collect(Collectors.toList());
}

private List<SubItem> extractActiveChildren(List<Item> items) {
    return items.stream()
        .flatMap(item -> item.getChildren().stream())
        .filter(SubItem::isActive)
        .collect(Collectors.toList());
}

private List<Result> processChildren(List<SubItem> children) {
    return children.stream()
        .map(SubItem::process)
        .collect(Collectors.toList());
}
```

**Techniques**:
- Extract nested logic to helper methods
- Use early returns to flatten control flow
- Leverage streams for single-level transformations
- Pre-curate data before iteration

### 2. Early Returns
**Rule**: Validate and exit early. Guard clauses at function start.

**Anti-pattern (Java)**:
```java
public Result processUser(User user) {
    if (user != null) {
        if (user.isActive()) {
            if (user.hasPermission()) {
                return performAction(user);
            } else {
                return null;
            }
        } else {
            return null;
        }
    } else {
        return null;
    }
}
```

**Correct pattern (Java)**:
```java
public Result processUser(User user) {
    if (user == null) {
        return null;
    }
    if (!user.isActive()) {
        return null;
    }
    if (!user.hasPermission()) {
        return null;
    }

    return performAction(user);
}
```

### 3. Single Responsibility Principle
**Rule**: Each function does exactly one thing. Name reflects that single purpose.

**Anti-pattern (Java)**:
```java
public List<User> processAndSaveUsers(List<User> users, Database db) {
    List<User> validated = new ArrayList<>();
    for (User user : users) {
        if (validateEmail(user.getEmail()) && validateAge(user.getAge())) {
            validated.add(user);
        }
    }

    for (User user : validated) {
        db.save(user);
        sendWelcomeEmail(user);
    }

    return validated;
}
```

**Correct pattern (Java)**:
```java
public List<User> processUsers(List<User> users) {
    return validateUsers(users);
}

private List<User> validateUsers(List<User> users) {
    return users.stream()
        .filter(this::isValidUser)
        .collect(Collectors.toList());
}

private boolean isValidUser(User user) {
    return validateEmail(user.getEmail()) && validateAge(user.getAge());
}

public void saveUsers(List<User> users, Database db) {
    users.forEach(db::save);
}

public void notifyUsers(List<User> users) {
    users.forEach(this::sendWelcomeEmail);
}
```

### 4. Branchless Paradigms
**Rule**: Minimize conditionals. Use data structures, lookups, and functional patterns.

**Anti-pattern (Java)**:
```java
public String getStatusMessage(String status) {
    if (status.equals("pending")) {
        return "Processing...";
    } else if (status.equals("complete")) {
        return "Done!";
    } else if (status.equals("failed")) {
        return "Error occurred";
    } else {
        return "Unknown";
    }
}
```

**Correct pattern (Java)**:
```java
private static final Map<String, String> STATUS_MESSAGES = Map.of(
    "pending", "Processing...",
    "complete", "Done!",
    "failed", "Error occurred"
);

public String getStatusMessage(String status) {
    return STATUS_MESSAGES.getOrDefault(status, "Unknown");
}
```

**Advanced branchless (Java)**:
```java
// Instead of conditionals, use data-driven selection
private static final Map<String, Double> DISCOUNT_RATES = Map.of(
    "vip", 0.2,
    "regular", 0.1,
    "new", 0.05
);

public double applyDiscount(double price, String customerType) {
    double rate = DISCOUNT_RATES.getOrDefault(customerType, 0.0);
    return price * (1 - rate);
}
```

### 5. 1D Data Iteration (Avoid Nested Loops)
**Rule**: Transform data in stacked passes, not nested iterations.

**Anti-pattern (Java)**:
```java
public List<OrderProduct> matchOrdersToProducts(List<Order> orders, List<Product> products) {
    List<OrderProduct> results = new ArrayList<>();
    for (Order order : orders) {
        for (Product product : products) {
            if (order.getProductId() == product.getId()) {
                results.add(new OrderProduct(order, product));
            }
        }
    }
    return results;
}
```

**Correct pattern (Java)**:
```java
public List<OrderProduct> matchOrdersToProducts(List<Order> orders, List<Product> products) {
    Map<Integer, Product> productMap = buildProductMap(products);
    return matchOrders(orders, productMap);
}

private Map<Integer, Product> buildProductMap(List<Product> products) {
    return products.stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));
}

private List<OrderProduct> matchOrders(List<Order> orders, Map<Integer, Product> productMap) {
    return orders.stream()
        .filter(order -> productMap.containsKey(order.getProductId()))
        .map(order -> new OrderProduct(order, productMap.get(order.getProductId())))
        .collect(Collectors.toList());
}
```

**Key technique**: Pre-curate lookup structures (maps, sets, indices) to eliminate nested scans.

### 6. Pre-emptive State Management
**Rule**: Prepare optimal data structures before iteration. Avoid repeated index access or nested lookups in loops.

**Anti-pattern (Java)**:
```java
public Map<Integer, Double> calculateTotals(List<Transaction> transactions, List<Account> accounts) {
    Map<Integer, Double> totals = new HashMap<>();
    for (Transaction txn : transactions) {
        Account account = null;
        for (Account acc : accounts) {  // Nested lookup!
            if (acc.getId() == txn.getAccountId()) {
                account = acc;
                break;
            }
        }
        if (account != null) {
            totals.merge(account.getId(), txn.getAmount(), Double::sum);
        }
    }
    return totals;
}
```

**Correct pattern (Java)**:
```java
public Map<Integer, Double> calculateTotals(List<Transaction> transactions, List<Account> accounts) {
    Map<Integer, Account> accountMap = indexAccounts(accounts);
    return aggregateByAccount(transactions, accountMap);
}

private Map<Integer, Account> indexAccounts(List<Account> accounts) {
    return accounts.stream()
        .collect(Collectors.toMap(Account::getId, Function.identity()));
}

private Map<Integer, Double> aggregateByAccount(List<Transaction> transactions, Map<Integer, Account> accountMap) {
    return transactions.stream()
        .filter(txn -> accountMap.containsKey(txn.getAccountId()))
        .collect(Collectors.groupingBy(
            Transaction::getAccountId,
            Collectors.summingDouble(Transaction::getAmount)
        ));
}
```

### 7. Succinct Function Composition
**Rule**: Break complex operations into small, named, composable functions.

**Anti-pattern (Java)**:
```java
public List<Integer> process(List<Integer> data) {
    List<Integer> temp1 = data.stream().filter(x -> x > 0).collect(Collectors.toList());
    List<Integer> temp2 = temp1.stream().map(x -> x * 2).collect(Collectors.toList());
    List<Integer> temp3 = temp2.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    return temp3.stream().limit(10).collect(Collectors.toList());
}
```

**Correct pattern (Java)**:
```java
public List<Integer> process(List<Integer> data) {
    List<Integer> positive = filterPositive(data);
    List<Integer> doubled = doubleValues(positive);
    List<Integer> sortedDesc = sortDescending(doubled);
    return takeTop10(sortedDesc);
}

private List<Integer> filterPositive(List<Integer> values) {
    return values.stream()
        .filter(v -> v > 0)
        .collect(Collectors.toList());
}

private List<Integer> doubleValues(List<Integer> values) {
    return values.stream()
        .map(v -> v * 2)
        .collect(Collectors.toList());
}

private List<Integer> sortDescending(List<Integer> values) {
    return values.stream()
        .sorted(Comparator.reverseOrder())
        .collect(Collectors.toList());
}

private List<Integer> takeTop10(List<Integer> values) {
    return values.stream()
        .limit(10)
        .collect(Collectors.toList());
}
```

### 8. Variable Staging
**Rule**: Stage transformed data in clearly-named variables for immediate next-line consumption.

**Anti-pattern (Java)**:
```java
Result result = calculate(transform(filter(normalize(data))));
```

**Correct pattern (Java)**:
```java
Data normalizedData = normalize(data);
Data filteredData = filter(normalizedData);
Data transformedData = transform(filteredData);
Result result = calculate(transformedData);
```

### 9. Options Pattern
**Rule**: Bundle related parameters into structured options payloads. Avoid parameter stuffing.

**Anti-pattern (Java)**:
```java
public void createUser(String name, String email, int age, String address,
                      String phone, String role, String department,
                      String manager, String startDate) {
    // 9 parameters - unmaintainable!
}
```

**Correct pattern (Java)**:
```java
@Data
public class PersonalInfo {
    private final String name;
    private final String email;
    private final int age;
    private final String address;
    private final String phone;
}

@Data
public class EmploymentInfo {
    private final String role;
    private final String department;
    private final String manager;
    private final String startDate;
}

@Data
public class UserOptions {
    private final PersonalInfo personal;
    private final EmploymentInfo employment;
}

public void createUser(UserOptions options) {
    validatePersonalInfo(options.getPersonal());
    validateEmploymentInfo(options.getEmployment());
    persistUser(options);
}
```

### 10. Named Bundles Over Separate Properties
**Rule**: Group related data into named structures, not loose tuples or separate variables.

**Anti-pattern (Java)**:
```java
// Returning multiple values via array or list - positional dependency
public Object[] getUserData(int userId) {
    return new Object[]{name, email, age, address};
}

Object[] data = getUserData(123);
String name = (String) data[0];  // Fragile positional access
```

**Correct pattern (Java)**:
```java
@Data
public class UserData {
    private final String name;
    private final String email;
    private final int age;
    private final String address;
}

public UserData getUserData(int userId) {
    return new UserData(name, email, age, address);
}

UserData user = getUserData(123);
System.out.println(user.getName());  // Named access
```

### 11. Chain/Fluent Pattern
**Rule**: Build chainable interfaces for sequential transformations.

**Anti-pattern (Java)**:
```java
Data data = loadData();
data = filterData(data);
data = transformData(data);
data = aggregateData(data);
Result result = exportData(data);
```

**Correct pattern (Java)**:
```java
public class DataPipeline {
    private List<Data> data;

    public DataPipeline(List<Data> data) {
        this.data = data;
    }

    public DataPipeline filter(Predicate<Data> predicate) {
        this.data = data.stream()
            .filter(predicate)
            .collect(Collectors.toList());
        return this;
    }

    public DataPipeline transform(Function<Data, Data> func) {
        this.data = data.stream()
            .map(func)
            .collect(Collectors.toList());
        return this;
    }

    public DataPipeline aggregate(Function<List<Data>, List<Data>> reducer) {
        this.data = reducer.apply(this.data);
        return this;
    }

    public List<Data> export() {
        return this.data;
    }
}

Result result = new DataPipeline(loadData())
    .filter(x -> x.getValue() > 0)
    .transform(x -> x.multiply(2))
    .aggregate(this::sum)
    .export();
```

### 12. Open/Closed Principle with Options
**Rule**: Design for extension via configuration, not modification.

**Correct pattern (Java)**:
```java
@Data
public class ProcessorOptions {
    private final List<Function<Data, Data>> validators;
    private final List<Function<Data, Data>> transformers;
    private final List<Function<Data, Data>> exporters;

    public ProcessorOptions() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public ProcessorOptions(
        List<Function<Data, Data>> validators,
        List<Function<Data, Data>> transformers,
        List<Function<Data, Data>> exporters
    ) {
        this.validators = validators;
        this.transformers = transformers;
        this.exporters = exporters;
    }
}

public class DataProcessor {
    private final ProcessorOptions options;

    public DataProcessor(ProcessorOptions options) {
        this.options = options;
    }

    public Data process(Data data) {
        Data validated = validate(data);
        Data transformed = transform(validated);
        return export(transformed);
    }

    private Data validate(Data data) {
        Data result = data;
        for (Function<Data, Data> validator : options.getValidators()) {
            result = validator.apply(result);
        }
        return result;
    }

    private Data transform(Data data) {
        Data result = data;
        for (Function<Data, Data> transformer : options.getTransformers()) {
            result = transformer.apply(result);
        }
        return result;
    }

    private Data export(Data data) {
        Data result = data;
        for (Function<Data, Data> exporter : options.getExporters()) {
            result = exporter.apply(result);
        }
        return result;
    }
}

// Extend via configuration, not modification
ProcessorOptions options = new ProcessorOptions(
    List.of(this::validateSchema, this::validateRange),
    List.of(this::normalize, this::scale),
    List.of(this::toJson)
);
DataProcessor processor = new DataProcessor(options);
```

## Refactoring Workflow

When you encounter code violating these principles:

1. **Identify Violations**
   - Nested loops → 1D iteration needed
   - Multiple params → Options pattern needed
   - Deep nesting → Early returns + extraction needed
   - Complex conditionals → Branchless approach needed

2. **Plan Refactoring**
   - List functions to extract
   - Identify data structures to pre-curate
   - Design options bundles
   - Map transformation pipeline

3. **Execute Refactoring**
   - Extract nested logic to named methods
   - Convert to early returns
   - Build lookup structures for 1D iteration
   - Create options classes
   - Implement chain/fluent where beneficial

4. **Verify**
   - Check: Maximum 1-level nesting
   - Check: No parameter stuffing (use options)
   - Check: No nested loops (use pre-curated state)
   - Check: Single responsibility per method
   - Check: Named bundles, not loose arrays/collections

## Code Review Checklist

Before approving any code:

- [ ] **Nesting**: Max 1 level of indentation in method bodies
- [ ] **Early Returns**: Guard clauses at top, early exits for invalid states
- [ ] **Single Responsibility**: Each method has one clear purpose
- [ ] **Branchless**: Conditionals minimized via data structures/lookups
- [ ] **1D Iteration**: No nested loops; pre-curated state for matching/lookups
- [ ] **Pre-emptive State**: Data structures built before iteration
- [ ] **Succinct Functions**: Complex operations broken into small, named steps
- [ ] **Variable Staging**: Transformations staged in named variables
- [ ] **Options Pattern**: Parameter bundles for >3 related parameters
- [ ] **Named Bundles**: Structured data, not loose arrays or separate vars
- [ ] **Chain/Fluent**: Pipeline operations use chainable interface where applicable

## Implementation Guidelines

### For New Code:
1. Design data flow as stacked passes
2. Create options bundles upfront for complex configurations
3. Use Lombok's `@Data` for structured data classes
4. Implement chain/fluent for pipelines
5. Extract methods liberally (prefer 5 small over 1 large)

### For Refactoring:
1. Identify deepest nesting first
2. Extract inner logic to methods
3. Convert conditionals to lookups (Map-based)
4. Replace nested loops with indexed lookups
5. Bundle parameters into options classes

### For Code Review:
1. Run through checklist above
2. Suggest specific refactorings for violations
3. Provide corrected code examples
4. Explain why the pattern improves maintainability

## Tool Access

This skill has pre-approved access to:
- **Read**: Examine code for convention violations
- **Write**: Create new files following conventions
- **Edit**: Refactor existing code to meet standards
- **Grep**: Find anti-patterns across codebase
- **Glob**: Locate files needing refactoring

Apply these conventions rigorously. Prioritize readability and maintainability. Every method should be immediately understandable, testable in isolation, and composable with others.

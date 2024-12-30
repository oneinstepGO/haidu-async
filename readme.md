# Haidu-Async

A lightweight asynchronous task orchestration framework based on JDK's CompletableFuture.

[中文文档](README_zh.md)

## Features

- **Flexible Task Orchestration**: Support for complex task dependencies and parallel execution
- **Configuration Driven**: Define task flows using simple JSON/YAML configuration
- **Robust Error Handling**: Built-in retry mechanism and timeout control
- **High Performance**: Efficient task scheduling based on CompletableFuture
- **Easy Integration**: Simple API design for seamless integration
- **Extensible**: Easy to extend with custom task implementations

## Quick Start

### 1. Add Dependency

```xml

<dependency>
  <groupId>com.oneinstep</groupId>
  <artifactId>haidu-async</artifactId>
  <version>1.0.0</version>
</dependency>
```

### 2. Define Tasks

```java
public class MyTask extends AbstractTask<String> {
  @Override
  protected Result<String> invoke(RequestContext context) {
    // Your task logic here
    return Result.success("Task completed");
  }
}
```

### 3. Configure Task Flow

```json
{
  "arrangeName": "demo-flow",
  "description": "Demo task flow",
  "arrangeRule": [
    [
      "taskA,taskB:taskC"
    ],
    [
      "taskD"
    ]
  ],
  "taskDetailsMap": {
    "taskA": {
      "taskId": "taskA",
      "fullClassName": "com.example.TaskA",
      "retries": 3,
      "timeout": 1000
    }
  }
}
```

### 4. Execute Tasks

```java
ExecutorService executorService = Executors.newFixedThreadPool(10);
TaskConfigFactory factory = new TaskConfigFactory(new JsonTaskDefinitionReader());
TaskConfig config = factory.createConfig(new FileReader("config.json"));

RequestContext context = new RequestContext();
context.

setTaskConfig(config);

TaskEngine engine = TaskEngine.getInstance(executorService);
engine.

startEngine(context);
```

## Documentation

- [Getting Started](docs/en/getting_started.md)
- [API Reference](docs/en/api_reference.md)
- [Configuration Guide](docs/en/configuration.md)
- [Examples](examples/)

## Advanced Features

- Task Parameter Support
  - Basic types (String, Integer, etc.)
  - Complex types (JSON, List, Map)
  - Context parameters
- Retry Mechanism
- Timeout Control
- Dependency Management
- Cycle Detection
- Thread Pool Management

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

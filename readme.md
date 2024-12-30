# Haidu-Async

A lightweight asynchronous task orchestration framework based on JDK's CompletableFuture.

[中文文档](README_zh.md)

## Core Features

- **Declarative Task Flow**: Define task dependencies using simple JSON/YAML configuration
- **Flexible Task Orchestration**: Support sequential, parallel, and complex dependency execution
- **Rich Parameter Types**: Support basic types, collections, JSON, and context parameters
- **Robust Error Handling**: Built-in retry mechanism and timeout control
- **High Performance**: Non-blocking execution based on CompletableFuture
- **Type Safety**: Generic task results with compile-time type checking

## Quick Start

### 1. Define Your Task

```java

@Slf4j
public class MyTask extends AbstractTask<String> {
  @Override
  protected Result<String> invoke(RequestContext context) {
    // Your task logic here
    return Result.success("Task completed");
  }

  @Override
  protected void beforeInvoke(RequestContext context) {
    // Pre-processing logic
  }

  @Override
  protected void afterInvoke(RequestContext context) {
    // Post-processing logic
  }

}
```

### 2. Configure Task Flow

```json
{
  "arrangeName": "demo-flow",
  "description": "Demo task flow",
  "arrangeRule": [
    [
      "taskA,taskB:taskC"
    ],
    // taskC depends on taskA and taskB
    [
      "taskD"
    ],
    // taskD executes after taskC
    [
      "taskE"
    ]
    // taskE executes after taskD
  ],
  "taskDetailsMap": {
    "taskA": {
      "taskId": "taskA",
      "fullClassName": "com.example.TaskA",
      "retries": 3,
      "timeout": 1000,
      "taskParams": [
        {
          "name": "param1",
          "type": "STRING",
          "value": "value1",
          "required": true
        }
      ]
    }
  }
}
```

### 3. Execute Tasks

```java
// Create executor service
ExecutorService executor = Executors.newFixedThreadPool(10);

// Load task configuration
TaskConfigFactory factory = new TaskConfigFactory(new JsonTaskDefinitionReader());
TaskConfig config = factory.createConfig(new FileReader("config.json"));

// Create and setup context
RequestContext context = new RequestContext();
context.

setTaskConfig(config);

// Execute task flow
TaskEngine engine = TaskEngine.getInstance(executor);
engine.

startEngine(context);

// Get results
Map<String, Result<?>> results = context.getTaskResultMap();
```

## Advanced Features

### Parameter Types

- Basic: STRING, INT, LONG, DOUBLE, BOOLEAN
- Complex: LIST, MAP, JSON, JSON_ARRAY
- Special: CONTEXT (runtime parameter resolution)

### Error Handling

- Configurable retry attempts
- Timeout control per task
- Custom error handlers

### Task Lifecycle

- beforeInvoke: Pre-processing hook
- invoke: Main task logic
- afterInvoke: Post-processing hook
- onError: Error handling hook
- onTimeout: Timeout handling hook

## Documentation

- [Getting Started](docs/en/getting_started.md)
- [Configuration Guide](docs/en/configuration.md)
- [API Reference](docs/en/api_reference.md)

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

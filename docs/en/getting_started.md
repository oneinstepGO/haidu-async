# Getting Started with Haidu-Async

## Prerequisites

- JDK 17 or higher
- Maven 3.6 or higher

## Installation

Add the following dependency to your pom.xml:

```xml
<dependency>
    <groupId>com.oneinstep</groupId>
    <artifactId>haidu-async</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Basic Usage

### 1. Define Task Classes

Create your task classes by extending AbstractTask:

```java
public class Task1 extends AbstractTask<String> {
    @Override
    protected Result<String> invoke(RequestContext context) {
        // Your task logic here
        return Result.success("Task1 Result");
    }

    @Override
    protected void beforeInvoke(RequestContext context) {
        // Pre-processing logic
    }

    @Override
    protected void afterInvoke(RequestContext context) {
        // Post-processing logic
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(Task1.class);
    }
}
```

### 2. Configure Task Dependencies

Create a JSON or YAML configuration file:

```json
{
  "arrangeName": "demo-task",
  "description": "Demo task arrangement",
  "arrangeRule": [
    [
      "task1,task2:task3"
    ],
    [
      "task4,task5",
      "task4:task6"
    ],
    [
      "task7"
    ]
  ],
  "taskDetailsMap": {
    "task1": {
      "taskId": "task1",
      "fullClassName": "com.example.Task1",
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
    // ... other task definitions
  }
}
```

### 3. Execute Tasks

```java
// Create executor service
ExecutorService executorService = Executors.newFixedThreadPool(10);

try {
    // Load task configuration
    TaskConfigFactory factory = new TaskConfigFactory(new JsonTaskDefinitionReader());
    TaskConfig taskConfig = factory.createConfig(new FileReader("config.json"));
    
    // Create request context
    RequestContext context = new RequestContext();
    context.setTaskConfig(taskConfig);
    
    // Execute tasks
    TaskEngine engine = TaskEngine.getInstance(executorService);
    engine.startEngine(context);
    
    // Get results
    Map<String, Result<?>> results = context.getTaskResultMap();
} finally {
    executorService.shutdown();
}
```

## Advanced Features

### Task Parameters

Haidu-Async supports various parameter types:

- STRING
- INT
- LONG
- DOUBLE
- BOOLEAN
- LIST
- MAP
- JSON
- JSON_ARRAY
- CONTEXT

### Retry Mechanism

Configure retry attempts in task details:

```json
{
  "retries": 3,
  "timeout": 1000
}
```

### Timeout Control

Set timeout duration in milliseconds:

```json
{
  "timeout": 5000
}
```

## Best Practices

1. Always set appropriate timeout values
2. Use meaningful task IDs
3. Implement proper error handling
4. Configure reasonable retry counts
5. Monitor task execution status

## Troubleshooting

Common issues and solutions... 
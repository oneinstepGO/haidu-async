# Getting Started with Haidu-Async

## Overview

Haidu-Async is designed for complex task orchestration scenarios such as:

- Data processing pipelines
- Distributed job workflows
- Service orchestration
- ETL processes

## Prerequisites

- JDK 17 or higher
- Maven 3.6 or higher
- Basic understanding of CompletableFuture

## Core Concepts

### Task

A task is the basic execution unit, defined by extending `AbstractTask<T>`:
```java
@Slf4j
public class DataProcessTask extends AbstractTask<ProcessResult> {
    @Override
    protected Result<ProcessResult> invoke(RequestContext context) {
        // Get parameters from context
        Map<String, Object> params = getParams();
        String inputData = (String) params.get("inputData");
        
        // Process data
        ProcessResult result = processData(inputData);
        
        return Result.success(result);
    }
    
    @Override
    protected void beforeInvoke(RequestContext context) {
        log.info("Starting data processing task: {}", getTaskId());
    }
    
    @Override
    protected void afterInvoke(RequestContext context) {
        log.info("Completed data processing task: {}", getTaskId());
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }
}
```

### Task Flow

Tasks are organized into flows using JSON/YAML configuration:
```json
{
  "arrangeName": "data-pipeline",
  "description": "Data processing pipeline",
  "arrangeRule": [
    ["fetchData,validateData:transformData"],  // Parallel fetch and validate, then transform
    ["processData,enrichData"],                // Parallel process and enrich
    ["saveData"]                               // Finally save
  ]
}
```

### Task Parameters

Parameters can be passed to tasks in various formats:

```json
{
  "taskParams": [
    {
      "name": "inputPath",
      "type": "STRING",
      "value": "/data/input.csv",
      "required": true
    },
    {
      "name": "config",
      "type": "JSON",
      "value": "{\"batch\":100,\"timeout\":5000}",
      "required": true
    },
    {
      "name": "userId",
      "type": "CONTEXT",
      "value": "#(current_user)#",
      "required": true
    }
  ]
}
```

## Common Use Cases

### 1. Sequential Tasks

```json
{
  "arrangeRule": [
    ["step1"],
    ["step2"],
    ["step3"]
  ]
}
```

### 2. Parallel Tasks
```json
{
  "arrangeRule": [
    ["taskA,taskB,taskC"]
  ]
}
```

### 3. Complex Dependencies
```json
{
  "arrangeRule": [
    ["prepare"],
    ["taskA,taskB:taskC", "taskD:taskE"],
    ["finalize"]
  ]
}
```

## Error Handling

### Retry Mechanism

```java
public class RetryableTask extends AbstractTask<String> {
    @Override
    protected void beforeInvoke(RequestContext context) {
        setRetryTimes(3);  // Set retry attempts
        setTimeout(5000L); // Set timeout in milliseconds
    }
    
    @Override
    protected void onError(RequestContext context, Throwable e) {
        log.error("Task failed: {}", e.getMessage());
        // Custom error handling
    }
}
```

### Timeout Handling

```java
@Override
protected void onTimeout(RequestContext context) {
    log.warn("Task {} timed out", getTaskId());
    // Cleanup resources
}
```

## Best Practices

1. Task Design
    - Keep tasks focused and single-purpose
    - Make tasks idempotent when possible
    - Use appropriate parameter types
    - Handle errors gracefully

2. Configuration
    - Use meaningful task IDs
    - Group related tasks together
    - Set reasonable timeouts
    - Document task dependencies

3. Performance
    - Configure thread pool size appropriately
    - Monitor task execution times
    - Use context parameters for shared data
    - Clean up resources in afterInvoke

4. Testing
    - Test tasks in isolation
    - Verify task dependencies
    - Test error scenarios
    - Test timeout handling

## Next Steps

- Read the [Configuration Guide](configuration.md) for detailed configuration options
- Check the [API Reference](api_reference.md) for complete API documentation
- Review example code in the repository 
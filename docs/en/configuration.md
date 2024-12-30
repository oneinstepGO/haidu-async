# Configuration Guide

Haidu-Async supports task orchestration configuration through JSON or YAML formats. This guide explains the
configuration file structure and rules in detail.

## Configuration Structure

The configuration file contains these main sections:

- arrangeName: Arrangement name
- description: Arrangement description
- arrangeRule: Arrangement rules
- taskDetailsMap: Task details mapping

### Basic Structure Example

```json
{
  "arrangeName": "example-task-flow",
  "description": "This is an example task flow configuration",
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
          "required": true,
          "description": "Parameter 1 description"
        }
      ]
    }
  }
}
```

## Arrangement Rules

### 1. Rule Format

Arrangement rules use simple line expressions:

- `task1,task2:task3` means task3 executes after both task1 and task2 complete
- Multiple rules can be combined, with order between groups

### 2. Rule Groups

arrangeRule is an array containing three types of groups:

1. Pre-tasks group (first group)
2. Parallel tasks groups (middle groups)
3. Post-tasks group (last group)

Example:

```json
"arrangeRule": [
["task1,task2:task3"],     // Pre-tasks
[
"task4,task5:task6", // Parallel tasks
"task7,task8:task9"
],
["task10"]                 // Post-tasks
]
```

### 3. Dependency Expression

- Parallel execution: Separated by commas, e.g., `task1,task2`
- Dependencies: Separated by colons, e.g., `task1:task2` means task2 depends on task1
- Compound relations: e.g., `task1,task2:task3` means task3 depends on both task1 and task2

## Task Details Configuration

### 1. Basic Properties

```json
{
  "taskId": "task1",
  // Task ID, must be unique
  "fullClassName": "com.example.Task1",
  // Full class name
  "retries": 3,
  // Retry count, optional, default 0
  "timeout": 1000
  // Timeout in milliseconds, optional, default 1000
}
```

### 2. Parameter Configuration

taskParams supports multiple parameter types:

```json
{
  "taskParams": [
    {
      "name": "strParam",
      // Parameter name
      "type": "STRING",
      // Parameter type
      "value": "value1",
      // Parameter value
      "required": true,
      // Is required
      "description": "Description"
      // Parameter description
    }
  ]
}
```

Supported parameter types:

- STRING: String
- INT: Integer
- LONG: Long integer
- DOUBLE: Double precision floating point
- BOOLEAN: Boolean value
- LIST: List, comma separated
- MAP: Map, format key1:value1,key2:value2
- JSON: JSON object
- JSON_ARRAY: JSON array
- CONTEXT: Context parameter, format #(key)#

### 3. Context Parameters

Context parameters allow retrieving values from RequestContext at runtime:

```json
{
  "taskParams": [
    {
      "name": "userId",
      "type": "CONTEXT",
      "value": "#(current_user_id)#",
      "required": true
    }
  ]
}
```

## YAML Format Example

The same configuration in YAML format:

```yaml
arrangeName: example-task-flow
description: This is an example task flow configuration
arrangeRule:
  - - "task1,task2:task3"
  - - "task4,task5"
    - "task4:task6"
  - - "task7"
taskDetailsMap:
  task1:
    taskId: task1
    fullClassName: com.example.Task1
    retries: 3
    timeout: 1000
    taskParams:
      - name: param1
        type: STRING
        value: value1
        required: true
        description: Parameter 1 description
```

## Best Practices

1. Task ID Naming
    - Use meaningful names
    - Consider module prefixes
    - Avoid special characters

2. Timeout Settings
    - Set reasonable timeouts based on task complexity
    - Consider network latency and dependent service response times
    - Set slightly longer than expected execution time

3. Retry Strategy
    - Only set retries for retryable operations
    - Avoid retrying non-idempotent operations
    - Recommend no more than 3 retries

4. Dependencies
    - Avoid complex dependency chains
    - Check for circular dependencies
    - Properly scope task granularity

## Common Issues

1. Circular Dependencies
    - Issue: Tasks form circular dependencies
    - Solution: Review and adjust task dependencies

2. Parameter Type Mismatch
    - Issue: Parameter values don't match declared types
    - Solution: Ensure values match declared types

3. Timeout Handling
    - Issue: Tasks frequently timeout
    - Solution: Adjust timeout values or optimize task execution 
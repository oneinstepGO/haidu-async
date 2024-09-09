# haidu-async

haidu-async 是一个用于管理和执行任务的引擎，支持任务的并行和依赖关系处理。

## 目录

- [功能特性](#功能特性)
- [使用方法](#使用方法)
- [配置](#配置)
- [示例](#示例)

## 功能特性

- **任务管理**：支持异步任务编排的定义与执行的分离，提高任务的可维护性，特别适合复杂任务的编排以及需要动态调整任务执行顺序的场景，例如商城首页数据的渲染。
- **任务执行**：支持任务的并行执行和依赖关系处理。
- **线程池管理**：支持自定义线程池，优化任务执行效率。

### 如何使用

#### 1、使用 json 定义规则，见 `src/test/resources/config/demo.task.config.json`

```json
{
  "arrangeRule": [
    [
      "1,2:3"
    ],
    [
      "1001,1002",
      "1001:1003",
      "1002:1005",
      "1001,1002:1004",
      "1003,1004,1005:1006"
    ],
    [
      "9998,9999"
    ]
  ],
  "taskDetailsMap": {
    "1": {
      "taskId": "1",
      "fullClassName": "com.oneinstep.haidu.task.Task1",
      "params": {}
    },
    "...": {
      "taskId": "..."
    }
  }
}
```

#### 2、使用 TaskEngine 执行任务

```java
  TaskConfig taskConfig = parseTaskConfig(task_config_json);
RequestContext requestContext = new RequestContext();
  requestContext.

setTaskConfig(taskConfig);

// 获取任务执行器
TaskEngine taskEngine = TaskEngine.getInstance(executorService);
// 启动任务引擎
  taskEngine.

startEngine(requestContext);
```

#### 3、获取任务执行结果

```java
  // 获取任务执行结果
TaskResult taskResult = taskEngine.getTaskResult();
// 获取任务执行结果
Map<String, TaskResult> taskResultMap = taskEngine.getTaskResultMap();
Result result1 = taskResultMap.get("1");
// ...
```

### 规则说明

一下面为例：

```json
{
  "arrangeRule": [
    [
      "1,2:3"
    ],
    [
      "1001,1002",
      "1001:1003",
      "1002:1005",
      "1001,1002:1004",
      "1003,1004,1005:1006"
    ],
    [
      "9998,9999"
    ]
  ],
  "taskDetailsMap": {
    "1": {
      "taskId": "1",
      "fullClassName": "com.oneinstep.haidu.task.Task1",
      "params": {}
    },
    "...": {
      "taskId": "..."
    }
  }
}
```

- `arrangeRule`：任务执行规则，二维数组
    - 第一个数组表示**前置任务**，在所有任务执行之前执行
    - 最后一个数组表示**后置任务**，在所有任务执行之后执行
    - 中间的数组表示**并行任务**，数组中的任务可以并行执行
        - 数组中的每一行表示一条依赖规则，并行任务用逗号分隔，有依赖关系的任务用冒号分隔，冒号后面的任务依赖冒号前面的任务
            - 依赖也有两种写法，例如 `["task1,task2:task3,task4"]` 表示 task1 和 task2 并行执行，task3 和 task4 依赖 task1 和
              task2
            - 也可以拆成两行，例如 `["task1,task2:task3", "task1,task2:task4"]`
    - 不可以出现循环依赖，否则会抛出 `IllegalTaskConfigException` 异常
- `taskDetailsMap`：任务详情，每个任务的详细信息，包括任务 ID、任务类全路径、任务参数等。

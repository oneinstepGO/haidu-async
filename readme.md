# haidu-async

haidu-async 是一个超轻量级的异步任务编排小组件，支持任务的并发和依赖关系处理，适用于复杂任务的编排以及需要动态调整任务执行顺序及内容的场景。

> 我们有时会遇到需要将一个大任务拆分成多个小任务，并且这些任务之间可能有依赖关系，然后并行执行，最后将所有小任务的结果合并，这时候就可以使用 haidu-async 来实现。
> 看下面这个任务
> 1. 任务 1001 和任务 1002 并行执行，任务 1003 依赖任务 1001，任务 1005 依赖任务 1002，任务 1004 同时依赖任务 1001 和任务 1002
> 2. 任务 1006 依赖任务 1003、1004 和 1005
> 
> ![](https://mp-img-1300842660.cos.ap-guangzhou.myqcloud.com/1725953990673-090b7cf8-dab5-4fce-99d2-f78a8bf5aa69.png)
> 
> 当然，你可以使用 CompletableFuture 来实现这个任务，但是这样的代码会比较复杂，而且不易维护，因为任务的依赖关系可能会随着业务的变化而变化。
> 如果，我们使用 haidu-async 来实现这个任务，那么我们只需要定义一个 json 文件，然后调用 haidu-async 的 API 即可实现这个任务。
## 目录

- [功能特性](#功能特性)
- [使用方法](#使用方法)
- [配置](#配置)
- [示例](#示例)

## 功能特性

- **任务管理**：支持异步任务编排的定义与执行的分离，提高任务的可维护性，特别适合复杂任务的编排以及需要动态调整任务执行顺序的场景，例如商城首页数据的渲染。
- **任务执行**：支持任务的并行执行和依赖关系处理，依赖循环检测，重试和超时机制。
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

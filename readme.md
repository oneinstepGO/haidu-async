# haidu-async

`haidu-async` 是一个`超轻量级`的`异步任务编排小工具`，主要基于 JDK 的`CompletableFuture`， 支持任务的多线程并发处理和依赖关系处理，支持
**任务编排定义和执行的分离**，适用于复杂任务的编排以及需要**动态调整任务**执行顺序及内容的场景。

> 引言：我们有时会遇到需要将一个大任务拆分成多个小任务（这些任务之间可能有依赖关系），然后并行执行，最后将所有小任务的结果合并，这时候就可以使用
> haidu-async 来实现。
> 看下面这个任务：
> 1. 任务 1001 和任务 1002 并行执行，任务 1003 依赖任务 1001，任务 1005 依赖任务 1002，任务 1004 同时依赖任务 1001 和任务
     1002；
> 2. 任务 1006 依赖任务 1003、1004 和 1005
>
> ![](https://mp-img-1300842660.cos.ap-guangzhou.myqcloud.com/1725953990673-090b7cf8-dab5-4fce-99d2-f78a8bf5aa69.png)
>
> 当然，你可以使用 CompletableFuture 硬编码来编排这个任务，但是这样的代码会比较复杂，而且不易维护，因为任务的依赖关系可能会随着业务的变化而变化。如果项目中有多处类似使用场景会
> 导致大量重复代码，令代码变得臃肿和难以维护，另外，参数如何传递也是一个难题。
> 但是如果我们使用 `haidu-async` 来实现这个需求，那么就十分简单，只需先定义一个任务编排规则（Json 或者 yaml 文件），然后调用
`haidu-async` 的一些方法即可实现。

### 功能特性

- **任务管理**：支持异步`任务编排的定义与执行的分离`，提高任务的`可维护性`，特别适合复杂任务的编排以及需要`动态调整任务`
  执行顺序的场景，例如app营销页的数据渲染。
- **简化配置、动态化**：使用简单`行表达式`定义任务执行规则，任务参数可实时传入，框架自行解析，并且支持任务的动态调整。
- **任务执行**：支持任务的并行执行和依赖关系处理，`循环依赖检测`，`重试`和`超时`机制。
- **线程池管理**：支持自定义线程池，优化任务执行效率。

### 如何使用？

- 1、使用 `json/yaml` 格式定义任务编排规则，见 `src/test/resources/config/demo.task.config.json`

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

- 2、使用 TaskEngine 执行任务

```java
// 获取任务规则配置
String fileName = Objects.requireNonNull(getClass().getClassLoader().getResource("config/demo.task.config.json")).getFile();
TaskConfigFactory factory = new TaskConfigFactory(new JsonTaskDefinitionReader());
// 构建任务执行上下文
RequestContext requestContext = new RequestContext();
requestContext.setTaskConfig(taskConfig);
// 获取任务执行器
TaskEngine taskEngine = TaskEngine.getInstance(executorService);
// 启动任务引擎
taskEngine.startEngine(requestContext);
```

- 3、获取任务执行结果

```java
// 获取任务执行结果
TaskResult taskResult = taskEngine.getTaskResult();
Map<String, TaskResult> taskResultMap = taskEngine.getTaskResultMap();
Result result1 = taskResultMap.get("1");
// ...
```

### 任务规则定义说明

下面以`json`配置为例，`yaml` 格式各配置项是一样的。

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

配置项说明：

- `arrangeRule`：任务执行规则，二维数组
    - 第一个数组元素表示**前置任务组**，意味着该组任务在所有`任务组`执行之前执行；
    - 最后一个数组元素表示**后置任务组**，在所有`任务组`执行之后执行；
    - 中间的数组元素表示**并行任务组**，数组中的多个任务组可以并行执行；
        - 数组中的每一行表示一条任务执行和依赖规则，并行任务用逗号分隔，有依赖关系的任务用冒号分隔，冒号后面的任务（子任务）依赖冒号前面的任务（父任务）
            - 依赖也有两种写法，例如 `["task1,task2:task3,task4"]` 表示 task1 和 task2 并行执行，task3,task4也并行执行，但是
              task3 和 task4 都依赖 task1 和
              task2 全都执行完毕；
            - 也可以拆成两行来表示，例如 `["task1,task2:task3", "task1,task2:task4"]`。
    - 不可以出现循环依赖，否则会抛出 `IllegalTaskConfigException` 异常。
- `taskDetailsMap`：任务详情，每个任务的详细信息，包括任务ID（必须）、任务类全路径（必须）、任务参数等。

### 读取配置文件方法

支持两种读取配置文件的方式，一种是 `json` 格式，一种是 `yaml` 格式，可以根据自己的喜好选择。

```java
// json 格式
TaskConfigFactory factory = new TaskConfigFactory(new JsonTaskDefinitionReader());
try {
      this.taskConfig = factory.createConfig(new FileReader(fileName));
} catch (Exception e) {
      throw new RuntimeException(e);
}

// yaml 格式
TaskConfigFactory factory = new TaskConfigFactory(new YamlTaskDefinitionReader());
try {
      this.taskConfig = factory.createConfig(new FileReader(fileName));
} catch (Exception e) {
      throw new RuntimeException(e);
}
```

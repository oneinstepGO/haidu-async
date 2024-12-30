# Haidu-Async

基于 JDK CompletableFuture 的轻量级异步任务编排框架。

[English](README.md)

## 核心特性

- **声明式任务流**: 使用简单的 JSON/YAML 配置定义任务依赖
- **灵活的任务编排**: 支持顺序执行、并行执行和复杂依赖执行
- **丰富的参数类型**: 支持基础类型、集合类型、JSON 和上下文参数
- **强大的错误处理**: 内置重试机制和超时控制
- **高性能**: 基于 CompletableFuture 的非阻塞执行
- **类型安全**: 泛型任务结果，支持编译时类型检查

## 快速开始

### 1. 定义任务

```java

@Slf4j
public class MyTask extends AbstractTask<String> {
    @Override
    protected Result<String> invoke(RequestContext context) {
        // 任务逻辑
        return Result.success("任务完成");
    }

    @Override
    protected void beforeInvoke(RequestContext context) {
        // 前置处理
    }

    @Override
    protected void afterInvoke(RequestContext context) {
        // 后置处理
    }
}
```

### 2. 配置任务流

```json
{
  "arrangeName": "示例流程",
  "description": "示例任务流",
  "arrangeRule": [
    [
      "taskA,taskB:taskC"
    ],
    // taskC 依赖 taskA 和 taskB
    [
      "taskD"
    ],
    // taskD 在 taskC 之后执行
    [
      "taskE"
    ]
    // taskE 在 taskD 之后执行
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

### 3. 执行任务

```java
// 创建线程池
ExecutorService executor = Executors.newFixedThreadPool(10);

// 加载任务配置
TaskConfigFactory factory = new TaskConfigFactory(new JsonTaskDefinitionReader());
TaskConfig config = factory.createConfig(new FileReader("config.json"));

// 创建并设置上下文
RequestContext context = new RequestContext();
context.setTaskConfig(config);

// 执行任务流
TaskEngine engine = TaskEngine.getInstance(executor);
engine.startEngine(context);

// 获取结果
Map<String, Result<?>> results = context.getTaskResultMap();
```

## 高级特性

### 参数类型

- 基础类型：STRING, INT, LONG, DOUBLE, BOOLEAN
- 复杂类型：LIST, MAP, JSON, JSON_ARRAY
- 特殊类型：CONTEXT（运行时参数解析）

### 错误处理

- 可配置重试次数
- 任务级别超时控制
- 自定义错误处理器

### 任务生命周期

- beforeInvoke：前置处理钩子
- invoke：主要任务逻辑
- afterInvoke：后置处理钩子
- onError：错误处理钩子
- onTimeout：超时处理钩子

## 文档

- [入门指南](docs/zh/getting_started.md)
- [配置指南](docs/zh/configuration.md)
- [API 参考](docs/zh/api_reference.md)

## 贡献

欢迎贡献！请查看我们的[贡献指南](CONTRIBUTING.md)。

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。 
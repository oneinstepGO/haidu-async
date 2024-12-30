# Haidu-Async

基于 JDK CompletableFuture 的轻量级异步任务编排框架。

[English Documentation](README.md)

## 特性

- **灵活的任务编排**：支持复杂的任务依赖关系和并行执行
- **配置驱动**：使用简单的 JSON/YAML 配置定义任务流
- **强大的错误处理**：内置重试机制和超时控制
- **高性能**：基于 CompletableFuture 的高效任务调度
- **易于集成**：简单的 API 设计，无缝集成
- **可扩展**：易于扩展自定义任务实现

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.oneinstep</groupId>
    <artifactId>haidu-async</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 定义任务

```java
public class MyTask extends AbstractTask<String> {
    @Override
    protected Result<String> invoke(RequestContext context) {
        // 你的任务逻辑
        return Result.success("任务完成");
    }
}
```

### 3. 配置任务流

```json
{
  "arrangeName": "示例流程",
  "description": "示例任务流程",
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

### 4. 执行任务

```java
ExecutorService executorService = Executors.newFixedThreadPool(10);
TaskConfigFactory factory = new TaskConfigFactory(new JsonTaskDefinitionReader());
TaskConfig config = factory.createConfig(new FileReader("config.json"));

RequestContext context = new RequestContext();
context.setTaskConfig(config);

TaskEngine engine = TaskEngine.getInstance(executorService);
engine.startEngine(context);
```

## 文档

- [入门指南](docs/zh/getting_started.md)
- [API 参考](docs/zh/api_reference.md)
- [配置指南](docs/zh/configuration.md)
- [示例](examples/)

## 高级特性

- 任务参数支持
    - 基础类型（字符串、整数等）
    - 复杂类型（JSON、列表、映射）
    - 上下文参数
- 重试机制
- 超时控制
- 依赖管理
- 循环检测
- 线程池管理

## 贡献

欢迎贡献！请查看我们的[贡献指南](CONTRIBUTING.md)。

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。 
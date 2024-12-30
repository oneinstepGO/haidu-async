# Haidu-Async 入门指南

## 前置条件

- JDK 17 或更高版本
- Maven 3.6 或更高版本

## 安装

在 pom.xml 中添加以下依赖：

```xml
<dependency>
    <groupId>com.oneinstep</groupId>
    <artifactId>haidu-async</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 基本用法

### 1. 定义任务类

通过继承 AbstractTask 创建你的任务类：

```java
public class Task1 extends AbstractTask<String> {
    @Override
    protected Result<String> invoke(RequestContext context) {
        // 你的任务逻辑
        return Result.success("Task1 结果");
    }

    @Override
    protected void beforeInvoke(RequestContext context) {
        // 前置处理逻辑
    }

    @Override
    protected void afterInvoke(RequestContext context) {
        // 后置处理逻辑
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(Task1.class);
    }
}
```

### 2. 配置任务依赖

创建 JSON 或 YAML 配置文件：

```json
{
  "arrangeName": "demo-task",
  "description": "示例任务编排",
  "arrangeRule": [
    ["task1,task2:task3"],
    ["task4,task5", "task4:task6"],
    ["task7"]
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
    // ... 其他任务定义
  }
}
```

### 3. 执行任务

```java
// 创建线程池
ExecutorService executorService = Executors.newFixedThreadPool(10);

try {
    // 加载任务配置
    TaskConfigFactory factory = new TaskConfigFactory(new JsonTaskDefinitionReader());
    TaskConfig taskConfig = factory.createConfig(new FileReader("config.json"));
    
    // 创建请求上下文
    RequestContext context = new RequestContext();
    context.setTaskConfig(taskConfig);
    
    // 执行任务
    TaskEngine engine = TaskEngine.getInstance(executorService);
    engine.startEngine(context);
    
    // 获取结果
    Map<String, Result<?>> results = context.getTaskResultMap();
} finally {
    executorService.shutdown();
}
```

## 高级特性

### 任务参数

Haidu-Async 支持多种参数类型：

- STRING（字符串）
- INT（整数）
- LONG（长整数）
- DOUBLE（双精度浮点数）
- BOOLEAN（布尔值）
- LIST（列表）
- MAP（映射）
- JSON（JSON对象）
- JSON_ARRAY（JSON数组）
- CONTEXT（上下文参数）

### 重试机制

在任务详情中配置重试次数：

```json
{
  "retries": 3,
  "timeout": 1000
}
```

### 超时控制

设置超时时间（毫秒）：

```json
{
  "timeout": 5000
}
```

## 最佳实践

1. 始终设置合适的超时时间
2. 使用有意义的任务ID
3. 实现适当的错误处理
4. 配置合理的重试次数
5. 监控任务执行状态

## 常见问题

常见问题及解决方案... 
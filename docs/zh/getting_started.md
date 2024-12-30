# Haidu-Async 入门指南

## 概述

Haidu-Async 专为以下复杂任务编排场景设计：

- 数据处理管道
- 分布式作业流程
- 服务编排
- ETL 流程

## 环境要求

- JDK 17 或更高版本
- Maven 3.6 或更高版本
- 了解 CompletableFuture 基本概念

## 核心概念

### 任务定义

任务是基本执行单元，通过继承 `AbstractTask<T>` 实现：
```java
@Slf4j
public class DataProcessTask extends AbstractTask<ProcessResult> {
    @Override
    protected Result<ProcessResult> invoke(RequestContext context) {
        // 从上下文获取参数
        Map<String, Object> params = getParams();
        String inputData = (String) params.get("inputData");
        
        // 处理数据
        ProcessResult result = processData(inputData);
        
        return Result.success(result);
    }
    
    @Override
    protected void beforeInvoke(RequestContext context) {
        log.info("开始数据处理任务: {}", getTaskId());
    }
    
    @Override
    protected void afterInvoke(RequestContext context) {
        log.info("完成数据处理任务: {}", getTaskId());
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }
}
```

### 任务流配置

使用 JSON/YAML 配置任务流：
```json
{
  "arrangeName": "数据管道",
  "description": "数据处理流水线",
  "arrangeRule": [
    ["获取数据,验证数据:转换数据"],  // 并行获取和验证，然后转换
    ["处理数据,数据增强"],          // 并行处理和增强
    ["保存数据"]                   // 最后保存
  ]
}
```

### 任务参数

支持多种格式的参数传递：

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

## 常见使用场景

### 1. 顺序执行

```json
{
  "arrangeRule": [
    ["步骤1"],
    ["步骤2"],
    ["步骤3"]
  ]
}
```

### 2. 并行执行

```json
{
  "arrangeRule": [
    ["任务A,任务B,任务C"]
  ]
}
```

### 3. 复杂依赖

```json
{
  "arrangeRule": [
    ["准备"],
    ["任务A,任务B:任务C", "任务D:任务E"],
    ["完成"]
  ]
}
```

## 错误处理

### 重试机制
```java
public class RetryableTask extends AbstractTask<String> {
    @Override
    protected void beforeInvoke(RequestContext context) {
        setRetryTimes(3);  // 设置重试次数
        setTimeout(5000L); // 设置超时时间（毫秒）
    }
    
    @Override
    protected void onError(RequestContext context, Throwable e) {
        log.error("任务执行失败: {}", e.getMessage());
        // 自定义错误处理
    }
}
```

### 超时处理

```java
@Override
protected void onTimeout(RequestContext context) {
    log.warn("任务 {} 超时", getTaskId());
    // 清理资源
}
```

## 最佳实践

1. 任务设计
    - 保持任务功能单一
    - 尽可能使任务具有幂等性
    - 选择合适的参数类型
    - 优雅处理错误

2. 配置管理
    - 使用有意义的任务ID
    - 相关任务分组
    - 设置合理的超时时间
    - 文档化任务依赖关系

3. 性能优化
    - 合理配置线程池大小
    - 监控任务执行时间
    - 使用上下文参数共享数据
    - 在 afterInvoke 中清理资源

4. 测试策略
    - 独立测试任务
    - 验证任务依赖关系
    - 测试错误场景
    - 测试超时处理

## 实际应用示例

### 数据处理流水线

```java
@Slf4j
public class DataPipeline {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            // 加载配置
            TaskConfigFactory factory = new TaskConfigFactory(new JsonTaskDefinitionReader());
            TaskConfig config = factory.createConfig(new FileReader("data-pipeline.json"));
            
            // 设置上下文
            RequestContext context = new RequestContext();
            context.setTaskConfig(config);
            context.getRequestParam().put("batch_size", 1000);
            
            // 执行任务流
            TaskEngine engine = TaskEngine.getInstance(executor);
            engine.startEngine(context);
            
            // 处理结果
            Map<String, Result<?>> results = context.getTaskResultMap();
            processResults(results);
            
        } catch (Exception e) {
            log.error("数据处理失败", e);
        } finally {
            executor.shutdown();
        }
    }
}
```

## 常见问题解决

1. 任务超时
    - 检查任务执行时间
    - 调整超时设置
    - 优化任务逻辑

2. 内存溢出
    - 控制并发任务数量
    - 及时释放资源
    - 使用流式处理

3. 死锁问题
    - 检查任务依赖关系
    - 避免循环依赖
    - 设置合理的超时时间

## 下一步

- 阅读[配置指南](configuration.md)了解详细配置选项
- 查看[API 参考](api_reference.md)获取完整 API 文档
- 参考示例代码深入学习 
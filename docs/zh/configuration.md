# 配置指南

Haidu-Async 支持通过 JSON 或 YAML 格式配置任务编排规则。本指南将详细说明配置文件的结构和规则。

## 配置文件结构

配置文件包含以下主要部分：

- arrangeName: 编排名称
- description: 编排描述
- arrangeRule: 编排规则
- taskDetailsMap: 任务详情映射

### 基本结构示例

```json
{
  "arrangeName": "示例任务编排",
  "description": "这是一个示例任务编排配置",
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
          "required": true,
          "description": "参数1描述"
        }
      ]
    }
  }
}
```

## 编排规则说明

### 1. 规则格式

编排规则使用简单的行表达式定义，格式为：

- `task1,task2:task3` 表示 task1 和 task2 并行执行完成后，再执行 task3
- 多个规则可以组合使用，每组规则之间是有序的

### 2. 规则分组

arrangeRule 是一个数组，包含三种类型的分组：

1. 前置任务组（第一组）
2. 并行任务组（中间若干组）
3. 后置任务组（最后一组）

示例：

```json
"arrangeRule": [
  ["task1,task2:task3"],     // 前置任务组
  ["task4,task5:task6",      // 并行任务组
   "task7,task8:task9"],
  ["task10"]                 // 后置任务组
]
```

### 3. 依赖关系表示

- 并行执行：使用逗号分隔，如 `task1,task2`
- 依赖关系：使用冒号分隔，如 `task1:task2` 表示 task2 依赖 task1
- 复合关系：如 `task1,task2:task3` 表示 task3 依赖 task1 和 task2

## 任务详情配置

### 1. 基本属性

```json
{
  "taskId": "task1",              // 任务ID，必须唯一
  "fullClassName": "com.example.Task1", // 任务类的全限定名
  "retries": 3,                   // 重试次数，可选，默认0
  "timeout": 1000                 // 超时时间(毫秒)，可选，默认1000
}
```

### 2. 参数配置

taskParams 支持多种参数类型：

```json
{
  "taskParams": [
    {
      "name": "strParam",         // 参数名
      "type": "STRING",           // 参数类型
      "value": "value1",          // 参数值
      "required": true,           // 是否必需
      "description": "描述"       // 参数描述
    }
  ]
}
```

支持的参数类型：

- STRING：字符串
- INT：整数
- LONG：长整数
- DOUBLE：双精度浮点数
- BOOLEAN：布尔值
- LIST：列表，用逗号分隔
- MAP：映射，格式为 key1:value1,key2:value2
- JSON：JSON 对象
- JSON_ARRAY：JSON 数组
- CONTEXT：上下文参数，格式为 #(key)#

### 3. 上下文参数使用

上下文参数允许在运行时从 RequestContext 中获取值：

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

## YAML 格式示例

同样的配置也可以使用 YAML 格式：

```yaml
arrangeName: 示例任务编排
description: 这是一个示例任务编排配置
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
        description: 参数1描述
```

## 最佳实践

1. 任务ID命名规范
    - 使用有意义的名称
    - 建议使用模块前缀
    - 避免特殊字符

2. 超时设置
    - 根据任务复杂度设置合理的超时时间
    - 考虑网络延迟和依赖服务的响应时间
    - 建议设置比预期执行时间稍长的超时时间

3. 重试策略
    - 对于可重试的操作才设置重试
    - 避免对非幂等操作进行重试
    - 重试次数建议不超过3次

4. 依赖关系
    - 避免复杂的依赖链
    - 注意检测循环依赖
    - 合理划分任务粒度

## 常见问题

1. 循环依赖
    - 问题：任务间形成循环依赖
    - 解决：检查并调整任务依赖关系

2. 参数类型错误
    - 问题：参数值与声明的类型不匹配
    - 解决：确保参数值符合声明的类型

3. 超时处理
    - 问题：任务经常超时
    - 解决：调整超时时间或优化任务执行效率 
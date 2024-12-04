package com.oneinstep.haidu.core;

import com.alibaba.fastjson2.JSON;
import com.oneinstep.haidu.config.TaskConfig;
import com.oneinstep.haidu.config.TaskDetail;
import com.oneinstep.haidu.config.TaskParam;
import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.exception.IllegalTaskConfigException;
import com.oneinstep.haidu.parser.ExpressionParser;
import com.oneinstep.haidu.parser.TaskGraph;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 任务引擎类，用于管理和执行任务
 */
@Slf4j
public class TaskEngine {
    // 单例实例，使用 volatile 确保可见性和防止指令重排序
    private static volatile TaskEngine INSTANCE;
    // 线程池，用于并发执行任务
    private final ExecutorService taskThreadPool;

    // 私有构造函数，允许传入自定义线程池
    private TaskEngine(ExecutorService taskThreadPool) {
        this.taskThreadPool = taskThreadPool != null ? taskThreadPool : new ThreadPoolExecutor(
                100,
                100,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(128),
                new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 获取 TaskEngine 实例
     *
     * @param taskThreadPool 自定义线程池
     * @return TaskEngine 实例
     */
    public static TaskEngine getInstance(final ExecutorService taskThreadPool) {
        if (INSTANCE == null) {
            synchronized (TaskEngine.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TaskEngine(taskThreadPool);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 启动任务引擎
     *
     * @param context 请求上下文，包含任务配置
     */
    public void startEngine(RequestContext context) {
        // 检查是否已经启动过引擎
        if (context.isEngineStarted()) {
            throw new IllegalStateException("任务引擎已经启动!");
        }

        TaskConfig taskConfig = context.getTaskConfig();
        if (taskConfig == null) {
            log.warn("the task config is null.");
            throw new IllegalTaskConfigException("任务配置为空");
        }
        List<List<String>> arrange = taskConfig.getArrangeRule();
        if (CollectionUtils.isEmpty(arrange)) {
            log.warn("the task arrange is empty...");
            throw new IllegalTaskConfigException("任务编排为空");
        }

        // 处理任务参数
        handleTaskParams(taskConfig.getTaskDetailsMap(), context);

        // 第一组是前置任务,中间N个组是并行任务,最后一个组是后置任务
        // 前置任务
        List<String> preTasks = arrange.get(0);
        arrangeToOneFuture(ExpressionParser.parseExpressions(preTasks, context.getTaskInstanceMap(), taskConfig.getTaskDetailsMap()), context).join();

        // 并行任务
        if (arrange.size() > 2) {
            List<CompletableFuture<Void>> parallelFutures = new ArrayList<>();
            for (int i = 1; i < arrange.size() - 1; i++) {
                List<String> parallelTasks = arrange.get(i);
                CompletableFuture<Void> future = arrangeToOneFuture(ExpressionParser.parseExpressions(parallelTasks, context.getTaskInstanceMap(), taskConfig.getTaskDetailsMap()), context);
                parallelFutures.add(future);
            }
            // 等待所有并行任务完成
            CompletableFuture.allOf(parallelFutures.toArray(new CompletableFuture[0])).join();
        } else {
            log.warn("没有并行任务组...");
        }

        // 后置任务
        if (arrange.size() >= 2) {
            // 后置任务
            List<String> postTasks = arrange.get(arrange.size() - 1);
            arrangeToOneFuture(ExpressionParser.parseExpressions(postTasks, context.getTaskInstanceMap(), taskConfig.getTaskDetailsMap()), context).join();
        } else {
            log.warn("没有后置任务组...");
        }

        // 设置引擎启动标志位
        context.setEngineStarted(true);
        // clear task instance map
        context.clearTaskInstanceMap();
    }

    private void handleTaskParams(Map<String, TaskDetail> taskDetailsMap, RequestContext context) {
        taskDetailsMap.values().forEach(detail -> {
            Map<String, Object> params = handleTaskParams(detail.getTaskParams(), context);
            detail.setParams(params);
        });
    }

    public Map<String, Object> handleTaskParams(List<TaskParam> taskParams, RequestContext context) {
        if (CollectionUtils.isEmpty(taskParams)) {
            return new HashMap<>();
        }
        Map<String, Object> params = new HashMap<>();
        for (TaskParam taskParam : taskParams) {
            TaskParam.Type type = taskParam.getType();
            String value = taskParam.getValue();
            Boolean required = taskParam.getRequired();
            if (Boolean.TRUE.equals(required)) {
                // 从 Context 中获取的参数在任务执行时处理
                if (StringUtils.isEmpty(value) && type != TaskParam.Type.CONTEXT) {
                    log.error("task param is required, but value is empty, taskParam:{}", taskParam);
                    throw new IllegalTaskConfigException("task param is required, but value is empty, taskParam:" + taskParam);
                }
            } else {
                if (StringUtils.isEmpty(value)) {
                    continue;
                }
            }
            try {
                value = value.trim();
                switch (type) {
                    case STRING:
                        params.put(taskParam.getName(), value);
                        break;
                    case INT:
                        params.put(taskParam.getName(), Integer.parseInt(value));
                        break;
                    case LONG:
                        params.put(taskParam.getName(), Long.parseLong(value));
                        break;
                    case DOUBLE:
                        params.put(taskParam.getName(), Double.parseDouble(value));
                        break;
                    case BOOLEAN:
                        params.put(taskParam.getName(), Boolean.parseBoolean(value));
                        break;
                    case LIST:
                        params.put(taskParam.getName(), value.split(","));
                        break;
                    case MAP:
                        String[] split = value.split(",");
                        Map<String, String> map = new HashMap<>();
                        for (String s : split) {
                            String[] kv = s.split(":");
                            map.put(kv[0], kv[1]);
                        }
                        params.put(taskParam.getName(), map);
                        break;
                    case JSON:
                        if (JSON.isValid(value)) {
                            params.put(taskParam.getName(), JSON.parse(value));
                        }
                        break;
                    case JSON_ARRAY:
                        if (JSON.isValidArray(value)) {
                            params.put(taskParam.getName(), JSON.parseArray(value));
                        }
                        break;
                    case CMS:
                        // 从CMS中获取
                        // TODO
                        break;
                    case CONTEXT:
                        // 从请求上下文中获取
                        // 任务执行时获取 value 为 key 的值
                        if (!(value.startsWith("#(") && value.endsWith(")#"))) {
                            log.error("task param value is not start with '\"#(' or end with ')#\"', taskParam:{}", taskParam);
                            throw new IllegalTaskConfigException("CONTEXT task param value must be start with '\"#(' and end with ')#\"', taskParam:" + taskParam);
                        }
                        params.put(taskParam.getName(), value);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                log.error("handle task params error, taskParam:{}", taskParam, e);
            }

        }
        return params;
    }

    /**
     * 执行任务图中的任务
     *
     * @param graph   任务图
     * @param context 请求上下文
     */
    private CompletableFuture<Void> arrangeToOneFuture(TaskGraph graph, RequestContext context) {
        // 存储任务节点及其对应的CompletableFuture
        Map<TaskGraph.TaskNode, CompletableFuture<Void>> futures = new HashMap<>();

        // 提交所有任务节点
        for (TaskGraph.TaskNode taskNode : graph.getAllTasks()) {
            submitTask(taskNode, futures, context);
        }

        // 等待所有任务完成
        return CompletableFuture.allOf(
                futures.values().toArray(new CompletableFuture[0])
        );
    }

    /**
     * 提交任务节点，并处理其依赖关系
     *
     * @param node    任务节点
     * @param futures 存储任务节点及其对应的CompletableFuture
     * @param context 请求上下文
     * @return 任务节点的CompletableFuture
     */
    private CompletableFuture<Void> submitTask(TaskGraph.TaskNode node, Map<TaskGraph.TaskNode, CompletableFuture<Void>> futures, RequestContext context) {
        // 如果任务节点已经提交过，则直接返回其CompletableFuture
        if (futures.containsKey(node)) {
            return futures.get(node);
        }

        // 提交依赖任务
        List<CompletableFuture<Void>> dependencyFutures = node.getDependencies().stream()
                .map(dep -> submitTask(dep, futures, context))
                .collect(Collectors.toList());

        // 提交当前任务，并在所有依赖任务完成后执行
        CompletableFuture<Void> taskFuture = CompletableFuture.allOf(
                dependencyFutures.toArray(new CompletableFuture[0])
        ).thenRunAsync(() -> node.getTask().accept(context), taskThreadPool);

        // 将任务节点及其CompletableFuture存储到futures中
        futures.put(node, taskFuture);
        return taskFuture;
    }
}

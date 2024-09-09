package com.oneinstep.haidu.core;

import com.oneinstep.haidu.config.TaskConfig;
import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.exception.IllegalTaskConfigException;
import com.oneinstep.haidu.parser.ExpressionParser;
import com.oneinstep.haidu.parser.TaskGraph;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

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

    /**
     * 运行时 task 类缓存，避免重复反射创建
     */
    private final Map<String, AbstractTask> taskInstanceMap = new ConcurrentHashMap<>();

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

        // 第一组是前置任务,中间N个组是并行任务,最后一个组是后置任务
        // 前置任务
        List<String> preTasks = arrange.get(0);
        arrangeToOneFuture(ExpressionParser.parseExpressions(preTasks, taskInstanceMap, taskConfig.getTaskDetailsMap()), context).join();

        // 并行任务
        if (arrange.size() > 2) {
            List<CompletableFuture<Void>> parallelFutures = new ArrayList<>();
            for (int i = 1; i < arrange.size() - 1; i++) {
                List<String> parallelTasks = arrange.get(i);
                CompletableFuture<Void> future = arrangeToOneFuture(ExpressionParser.parseExpressions(parallelTasks, taskInstanceMap, taskConfig.getTaskDetailsMap()), context);
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
            arrangeToOneFuture(ExpressionParser.parseExpressions(postTasks, taskInstanceMap, taskConfig.getTaskDetailsMap()), context).join();
        } else {
            log.warn("没有后置任务组...");
        }

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

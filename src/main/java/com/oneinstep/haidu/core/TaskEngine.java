package com.oneinstep.haidu.core;

import com.oneinstep.haidu.config.ExpressionParser;
import com.oneinstep.haidu.config.TaskConfig;
import com.oneinstep.haidu.config.TaskGraph;
import com.oneinstep.haidu.config.TaskNode;
import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.exception.IllegalTaskConfigException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 任务引擎
 */
@Slf4j
public class TaskEngine {
    // 单例
    private static TaskEngine INSTANCE;
    // 线程池
    private static ExecutorService taskThreadPool;

    /**
     * 运行时 task 类缓存，避免重复反射创建
     */
    private final Map<String, AbstractTask> taskInstanceMap = new ConcurrentHashMap<>();

    private TaskEngine() {
        // 默认线程池
        this.taskThreadPool = new ThreadPoolExecutor(
                100,
                100,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(128),
                new ThreadPoolExecutor.AbortPolicy());
    }

    private TaskEngine(ExecutorService taskThreadPool) {
        // 用户自定义线程池
        this.taskThreadPool = taskThreadPool;
    }

    // 实例获取方法
    public static TaskEngine getInstance(final ExecutorService taskThreadPool) {
        if (INSTANCE == null) {
            synchronized (TaskEngine.class) {
                if (INSTANCE == null) {
                    if (taskThreadPool == null) {
                        INSTANCE = new TaskEngine();
                    } else {
                        INSTANCE = new TaskEngine(taskThreadPool);
                    }
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 启动任务引擎
     *
     * @param context
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

        Map<String, String> taskClassNameMap = taskConfig.getTaskDetailsMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFullClassName()));

        context.setTaskClassNameMap(taskClassNameMap);

        // 第一个是前置任务
        // 中间的是并行任务
        // 最后一个是后置任务

        for (int i = 0; i < arrange.size(); i++) {
            List<String> tasks = arrange.get(i);
            TaskGraph graph = ExpressionParser.parseExpressions(tasks, taskInstanceMap, taskClassNameMap);
            execute(graph, context);
        }

    }

    public void execute(TaskGraph graph, RequestContext context) {
        Map<TaskNode, CompletableFuture<Void>> futures = new HashMap<>();

        for (TaskNode taskNode : graph.getAllTasks()) {
            submitTask(taskNode, futures, context);
        }

        // 等待所有任务完成
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                futures.values().toArray(new CompletableFuture[0])
        );
        allTasks.join();
    }

    private CompletableFuture<Void> submitTask(TaskNode node, Map<TaskNode, CompletableFuture<Void>> futures, RequestContext context) {
        if (futures.containsKey(node)) {
            return futures.get(node);
        }

        List<CompletableFuture<Void>> dependencyFutures = node.getDependencies().stream()
                .map(dep -> submitTask(dep, futures, context))
                .collect(Collectors.toList());

        CompletableFuture<Void> taskFuture = CompletableFuture.allOf(
                dependencyFutures.toArray(new CompletableFuture[0])
        ).thenRunAsync(() -> node.getTask().accept(context), taskThreadPool);

        futures.put(node, taskFuture);
        return taskFuture;
    }

}

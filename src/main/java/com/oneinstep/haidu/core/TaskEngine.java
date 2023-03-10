package com.oneinstep.haidu.core;

import com.oneinstep.haidu.config.TaskConfig;
import com.oneinstep.haidu.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private static final Map<String, AbstractTask> RUN_TIME_TASK_MAP = new ConcurrentHashMap<>();

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
     * @param context
     */
    public void startEngine(RequestContext context) {
        TaskConfig taskConfig = context.getTaskConfig();
        if (taskConfig == null) {
            log.warn("the task config is null.");
            return;
        }
        List<List<String>> arrange = taskConfig.getArrangeRule();
        if (CollectionUtils.isEmpty(arrange)) {
            log.warn("the task arrange is empty...");
            return;
        }
        Map<String, String> taskIdAndClassNameMap = taskConfig.getTaskDetailsMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFullClassName()));
        // 一行为一个编排组 保存编排组
        List<CompletableFuture<Void>> commonFutures = new ArrayList<>();
        for (int i = 0; i < arrange.size(); i++) {
            if (i == 0) {
                CompletableFuture<Void> beginFuture = arrangeGroup(arrange.get(i), context, taskIdAndClassNameMap);
                beginFuture.join();
            } else if (i == arrange.size() - 1) {
                CompletableFuture<Void> middleFuture = CompletableFuture.allOf(commonFutures.toArray(new CompletableFuture[0]));
                middleFuture.join();
                CompletableFuture<Void> lastFuture = arrangeGroup(arrange.get(i), context, taskIdAndClassNameMap);
                lastFuture.join();
            } else {
                commonFutures.add(arrangeGroup(arrange.get(i), context, taskIdAndClassNameMap));
            }
        }
    }

    private AbstractTask[] getTasks(String[] taskIdsArr, Map<String, String> taskClassNameMap) {
        return Stream.of(taskIdsArr).map(taskId -> {
            AbstractTask task = RUN_TIME_TASK_MAP.get(taskId);
            String taskClassName = taskClassNameMap.get(taskId);
            if (task != null && task.getClass().getSimpleName().equalsIgnoreCase(taskClassName))  {
                return task;
            }
            try {
                if (StringUtils.isBlank(taskClassName)) {
                    log.error("there is no task class name in the taskClassNameMap.");
                    throw new RuntimeException("无效taskClassName");
                }
                task = (AbstractTask) Class.forName(taskClassName).getConstructor().newInstance();
                task.setTaskId(taskId);

                RUN_TIME_TASK_MAP.put(taskId, task);
                return task;
            } catch (Exception e) {
                log.error("Instantiation Exception during taskId={}, taskName={}", taskId, taskClassName, e);
                throw new RuntimeException(e);
            }
        }).filter(Objects::nonNull).toArray(AbstractTask[]::new);
    }

    private CompletableFuture<Void> arrangeGroup(List<String> arrange, RequestContext taskContext, Map<String, String> taskNameMap) {
        // 映射 taskId -> future 存储Future
        Map<String, CompletableFuture<Void>> alreadySubmitFutureMap = new ConcurrentHashMap<>();
        // 一行为一个编排组 保存编排组
        CompletableFuture<Void> wholeFuture = null;
        for (int i = 0; i < arrange.size(); i++) {
            // 行编排  Future
            CompletableFuture<Void> lineFuture = null;
            String arrangeLine = arrange.get(i);
            String[] arrangeSegArr = arrangeLine.split(":");
            AbstractTask[] fatherTasks = getTasks(arrangeSegArr[0].split(","), taskNameMap);
            if (i == 0) {
                // 第一行 开始编排 直接存入 map
                List<CompletableFuture<Void>> startList = new ArrayList<>();
                Arrays.stream(fatherTasks).forEach(fatherTask -> {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> fatherTask.accept(taskContext), taskThreadPool);
                    startList.add(future);
                    alreadySubmitFutureMap.put(fatherTask.getTaskId(), future);
                });
                if (arrange.size() == 1) {
                    wholeFuture = CompletableFuture.allOf(startList.toArray(new CompletableFuture[0]));
                    break;
                }
            } else {
                if (fatherTasks.length == 1) {
                    lineFuture = Optional.of(alreadySubmitFutureMap.get(fatherTasks[0].getTaskId())).orElseThrow(() -> new RuntimeException("future not in start futures."));
                } else {
                    lineFuture = CompletableFuture.allOf(Arrays.stream(fatherTasks)
                            .map(fatherTask -> Optional.of(alreadySubmitFutureMap.get(fatherTask.getTaskId())).orElseThrow(() -> new RuntimeException("future not in start futures.")))
                            .toArray(CompletableFuture[]::new));
                }
            }
            if (arrangeSegArr.length > 1) {
                AbstractTask[] secondTasks = getTasks(new String[]{arrangeSegArr[1]}, taskNameMap);
                AbstractTask lineEndTask = secondTasks[0];
                lineFuture = lineFuture.thenAcceptAsync((r) -> lineEndTask.accept(taskContext), taskThreadPool);
                alreadySubmitFutureMap.put(lineEndTask.getTaskId(), lineFuture);
            }
            // 最后一行 结束编排
            if (i != 0 && i == arrange.size() - 1) {
                wholeFuture = lineFuture;
            }
        }
        alreadySubmitFutureMap.clear();
        return wholeFuture;
    }

}

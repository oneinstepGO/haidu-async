package com.oneinstep.haidu.core;

import com.oneinstep.haidu.context.RequestContext;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;

/**
 * 任务引擎
 */
@Slf4j
public abstract class AbstractTaskEngine {

    /**
     * 默认线程池
     */
    private volatile ExecutorService defaultExecutorService;

    /**
     * 获取 AbstractTask数组
     *
     * @param taskIds taskId数组
     * @return
     */
    protected abstract AbstractTask[] getTasks(String[] taskIds, Map<String, String> taskNameMap);

    protected abstract ExecutorService getExecutorService();


    /**
     * 获取默认线程池
     *
     * @return
     */
    public ExecutorService getDefaultExecutorService() {
        return defaultExecutorService;
    }


    /**
     * Task1,Task2 // 开始任务
     * Task1:Task3
     * Task2:Task5
     * Task1,Task2:Task4
     * Task3,Task4,Task5:Task6
     *
     * @param arrange     编排规则
     * @param taskContext 任务上下文
     */
    public CompletableFuture<Void> arrangeGroup(List<String> arrange, RequestContext taskContext, Map<String, String> taskNameMap) {

        // 获取线程池
        ExecutorService executorService = getUsedExecutorService();

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
                    CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> fatherTask.exec(taskContext), executorService);
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
                lineFuture = lineFuture.thenAcceptAsync((r) -> lineEndTask.exec(taskContext), executorService);
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

    /**
     * 获取使用的线程池
     *
     * @return
     */
    private ExecutorService getUsedExecutorService() {
        ExecutorService usedExecutorService;
        if (getExecutorService() == null) {
            // 初始化 默认线程池
            if (this.defaultExecutorService == null) {
                synchronized (this) {
                    if (this.getDefaultExecutorService() == null) {
                        log.info("create the default ExecutorService......");
                        this.defaultExecutorService = new ThreadPoolExecutor(
                                100,
                                100,
                                60,
                                TimeUnit.SECONDS,
                                new LinkedBlockingQueue<>(128),
                                new ThreadPoolExecutor.AbortPolicy());
                    }
                }
            }
            usedExecutorService = this.defaultExecutorService;
        } else {
            usedExecutorService = getExecutorService();
        }
        return usedExecutorService;
    }

}

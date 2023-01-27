package com.oneinstep.haidu.core;

import com.oneinstep.haidu.config.TaskConfig;
import com.oneinstep.haidu.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class TaskEngine extends AbstractTaskEngine {

    private ExecutorService taskThreadPool;

    public TaskEngine(ExecutorService taskThreadPool) {
        this.taskThreadPool = taskThreadPool;
    }

    /**
     * 运行时 task 类缓存，避免重复反射创建
     */
    private final Map<String, AbstractTask> runTimeTaskMap = new ConcurrentHashMap<>();

    public void taskArrangeAndExec(RequestContext context) {
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

        Map<String, String> taskIdAndClassNameMap = taskConfig.getTaskDetailsMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFullClassName()));

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

    @Override
    protected AbstractTask[] getTasks(String[] taskIdsArr, Map<String, String> taskClassNameMap) {
        return Stream.of(taskIdsArr).map(taskId -> {
            AbstractTask task = runTimeTaskMap.get(taskId);
            if (task != null) {
                return task;
            }
            String taskClassName = null;
            try {
                taskClassName = taskClassNameMap.get(taskId);
                if (StringUtils.isBlank(taskClassName)) {
                    log.error("there is no task class name in the taskClassNameMap.");
                    throw new RuntimeException("无效taskClassName");
                }
                task = (AbstractTask) Class.forName(taskClassName).getConstructor().newInstance();
                task.setTaskId(taskId);

                runTimeTaskMap.put(taskId, task);
                return task;
            } catch (Exception e) {
                log.error("Instantiation Exception during taskId={}, taskName={}", taskId, taskClassName, e);
                throw new RuntimeException(e);
            }
        }).filter(Objects::nonNull).toArray(AbstractTask[]::new);
    }

    @Override
    protected ExecutorService getExecutorService() {
        return taskThreadPool;
    }

}

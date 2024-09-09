package com.oneinstep.haidu.parser;

import com.oneinstep.haidu.config.TaskDetail;
import com.oneinstep.haidu.core.AbstractTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 任务表达式解析器
 */
@Slf4j
public class ExpressionParser {

    /**
     * 解析任务表达式，生成任务图
     *
     * @param expressions     任务表达式列表
     * @param taskInstanceMap 任务实例缓存
     * @param taskDetailMap   任务详情映射
     * @return 解析后的任务图
     */
    public static TaskGraph parseExpressions(List<String> expressions, Map<String, AbstractTask> taskInstanceMap, Map<String, TaskDetail> taskDetailMap) {
        TaskGraph graph = new TaskGraph();

        for (String expression : expressions) {
            // 解析任务表达式，获取并行任务和依赖任务
            String[] parts = expression.split(":");
            String[] parallelTasks = parts[0].split(",");

            for (String taskId : parallelTasks) {
                // 获取并存储任务实例
                AbstractTask andStoreTask = getAndStoreTask(taskId, taskInstanceMap, taskDetailMap);
                // 将任务添加到任务图中
                graph.addTask(taskId, andStoreTask);
            }

            if (parts.length > 1) {
                // 处理依赖任务
                String[] dependentTaskIds = parts[1].split(",");
                for (String dependentTaskId : dependentTaskIds) {
                    AbstractTask andStoreTask = getAndStoreTask(dependentTaskId, taskInstanceMap, taskDetailMap);
                    graph.addTask(dependentTaskId, andStoreTask);

                    for (String taskId : parallelTasks) {
                        // 添加任务依赖关系
                        graph.addDependency(dependentTaskId, taskId);
                    }
                }
            }
        }

        return graph;
    }

    /**
     * 获取并存储任务实例
     *
     * @param taskId          任务ID
     * @param taskInstanceMap 任务实例缓存
     * @param taskDetailMap   任务详情映射
     * @return 任务实例
     */
    public static AbstractTask getAndStoreTask(String taskId, Map<String, AbstractTask> taskInstanceMap, Map<String, TaskDetail> taskDetailMap) {
        // 从缓存中获取任务实例
        AbstractTask task = taskInstanceMap.get(taskId);
        TaskDetail taskDetail = taskDetailMap.get(taskId);
        String taskClassName = taskDetail.getFullClassName();

        Integer retryTimes = taskDetail.getRetries();
        Long timeout = taskDetail.getTimeout();
        Map<String, Object> params = taskDetail.getParams();

        // 如果缓存中存在且类名匹配，直接返回任务实例
        if (task != null && task.getClass().getSimpleName().equalsIgnoreCase(taskClassName)) {
            return task;
        }
        try {
            // 检查任务类名是否为空
            if (StringUtils.isBlank(taskClassName)) {
                log.error("there is no task class name in the taskClassNameMap.");
                throw new RuntimeException("无效taskClassName");
            }
            // 通过反射创建任务实例
            task = (AbstractTask) Class.forName(taskClassName).getDeclaredConstructor().newInstance();
            task.setTaskId(taskId);
            task.setRetryTimes(retryTimes);
            task.setTimeout(timeout);
            task.setParams(params);

            // 将任务实例存储到缓存中
            taskInstanceMap.put(taskId, task);
            return task;
        } catch (Exception e) {
            log.error("Instantiation Exception during taskId={}, taskName={}", taskId, taskClassName, e);
            throw new RuntimeException(e);
        }
    }
}

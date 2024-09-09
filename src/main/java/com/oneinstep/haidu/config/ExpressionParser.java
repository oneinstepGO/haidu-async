package com.oneinstep.haidu.config;

import com.oneinstep.haidu.core.AbstractTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
public class ExpressionParser {

    public static TaskGraph parseExpressions(List<String> expressions, Map<String, AbstractTask> taskInstanceMap, Map<String, String> taskClassNameMap) {
        TaskGraph graph = new TaskGraph();

        for (String expression : expressions) {
            String[] parts = expression.split(":");
            String[] parallelTasks = parts[0].split(",");

            for (String taskId : parallelTasks) {

                AbstractTask andStoreTask = getAndStoreTask(taskId, taskInstanceMap, taskClassNameMap);

                graph.addTask(taskId, andStoreTask);
            }

            if (parts.length > 1) {
                String dependentTaskId = parts[1];

                AbstractTask andStoreTask = getAndStoreTask(dependentTaskId, taskInstanceMap, taskClassNameMap);

                graph.addTask(dependentTaskId, andStoreTask);


                for (String taskId : parallelTasks) {
                    graph.addDependency(dependentTaskId, taskId);
                }
            }
        }

        return graph;
    }

    public static AbstractTask getAndStoreTask(String taskId, Map<String, AbstractTask> taskInstanceMap, Map<String, String> taskClassNameMap) {
        AbstractTask task = taskInstanceMap.get(taskId);
        String taskClassName = taskClassNameMap.get(taskId);
        if (task != null && task.getClass().getSimpleName().equalsIgnoreCase(taskClassName)) {
            return task;
        }
        try {
            if (StringUtils.isBlank(taskClassName)) {
                log.error("there is no task class name in the taskClassNameMap.");
                throw new RuntimeException("无效taskClassName");
            }
            task = (AbstractTask) Class.forName(taskClassName).getConstructor().newInstance();
            task.setTaskId(taskId);

            taskInstanceMap.put(taskId, task);
            return task;
        } catch (Exception e) {
            log.error("Instantiation Exception during taskId={}, taskName={}", taskId, taskClassName, e);
            throw new RuntimeException(e);
        }
    }
}

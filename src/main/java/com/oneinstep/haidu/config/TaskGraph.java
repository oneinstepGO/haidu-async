package com.oneinstep.haidu.config;

import com.oneinstep.haidu.core.AbstractTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskGraph {

    private Map<String, TaskNode> taskMap = new ConcurrentHashMap<>();

    public void addTask(String taskId, AbstractTask task) {
        taskMap.putIfAbsent(taskId, new TaskNode(taskId, task));
    }

    public void addDependency(String taskId, String dependencyId) {
        TaskNode task = taskMap.get(taskId);
        TaskNode dependency = taskMap.get(dependencyId);
        if (task != null && dependency != null) {
            task.dependencies.add(dependency);
        }
    }

    public List<TaskNode> getAllTasks() {
        return Collections.unmodifiableList(new ArrayList<>(taskMap.values()));
    }

}

package com.oneinstep.haidu.parser;

import com.oneinstep.haidu.core.AbstractTask;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务图类，用于管理任务及其依赖关系
 */
public class TaskGraph {

    // 使用并发哈希映射来存储任务节点，保证线程安全
    private Map<String, TaskNode> taskMap = new ConcurrentHashMap<>();

    /**
     * 添加任务到任务图中
     *
     * @param taskId 任务ID
     * @param task   任务实例
     */
    public void addTask(String taskId, AbstractTask task) {
        // 如果任务ID不存在，则添加新的任务节点
        taskMap.putIfAbsent(taskId, new TaskNode(taskId, task));
    }

    /**
     * 添加任务依赖关系
     *
     * @param taskId       任务ID
     * @param dependencyId 依赖任务ID
     */
    public void addDependency(String taskId, String dependencyId) {
        // 获取任务节点和依赖任务节点
        TaskNode task = taskMap.get(taskId);
        TaskNode dependency = taskMap.get(dependencyId);
        // 如果任务节点和依赖任务节点都存在，则将依赖任务添加到任务的依赖列表中
        if (task != null && dependency != null) {
            task.dependencies.add(dependency);
        }
    }

    /**
     * 获取所有任务节点
     *
     * @return 不可修改的任务节点列表
     */
    public List<TaskNode> getAllTasks() {
        // 返回任务节点列表的不可修改视图
        return List.copyOf(taskMap.values());
    }

    public List<String> getAllTaskIds() {
        return List.copyOf(taskMap.keySet());
    }

    public String[] getDependencies(String taskId) {
        TaskNode task = taskMap.get(taskId);
        if (task == null) {
            return new String[0];
        }
        return task.dependencies.stream().map(node -> node.taskId).toArray(String[]::new);
    }

    /**
     * 任务节点类，用于表示一个任务及其依赖关系
     */
    @Slf4j
    @Getter
    public static class TaskNode {

        // 任务ID
        String taskId;

        // 任务实例
        AbstractTask task;

        // 依赖任务列表
        List<TaskNode> dependencies = new ArrayList<>();

        /**
         * 构造函数，初始化任务节点
         *
         * @param taskId 任务ID
         * @param task   任务实例
         */
        TaskNode(String taskId, AbstractTask task) {
            this.taskId = taskId;
            this.task = task;
        }
    }

}

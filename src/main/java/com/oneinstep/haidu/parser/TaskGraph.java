package com.oneinstep.haidu.parser;

import com.oneinstep.haidu.core.AbstractTask;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务图类，用于管理任务及其依赖关系
 */
public class TaskGraph {

    /**
     * 任务节点映射，用于存储任务节点
     * key: 任务ID
     * value: 任务节点
     */
    private final Map<String, TaskNode> taskMap = new ConcurrentHashMap<>();

    /**
     * 添加任务到任务图中
     *
     * @param taskId 任务ID
     * @param task   任务实例
     */
    public void addTask(String taskId, AbstractTask<?> task) {
        // 添加任务节点时不要求与其他节点有连接
        taskMap.putIfAbsent(taskId, new TaskNode(taskId, task));
    }

    /**
     * 添加任务依赖关系
     *
     * @param taskId       任务ID
     * @param dependencyId 依赖任务ID
     */
    public void addDependency(String taskId, String dependencyId) {
        // 添加依赖关系是可选的
        TaskNode task = taskMap.get(taskId);
        TaskNode dependency = taskMap.get(dependencyId);
        if (task != null && dependency != null) {
            if (task.equals(dependency)) {
                throw new IllegalArgumentException("Task cannot depend on itself: " + taskId);
            }
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

    /**
     * 获取所有任务ID
     *
     * @return 任务ID列表
     */
    public List<String> getAllTaskIds() {
        return List.copyOf(taskMap.keySet());
    }

    /**
     * 获取任务的依赖任务ID列表
     *
     * @param taskId 任务ID
     * @return 依赖任务ID列表
     */
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
        AbstractTask<?> task;

        // 依赖任务列表
        Set<TaskNode> dependencies = new HashSet<>();

        /**
         * 构造函数，初始化任务节点
         *
         * @param taskId 任务ID
         * @param task   任务实例
         */
        TaskNode(String taskId, AbstractTask<?> task) {
            this.taskId = taskId;
            this.task = task;
        }
    }

}

package com.oneinstep.haidu.config;

import lombok.experimental.UtilityClass;

import java.util.*;

/**
 * 任务依赖检查器
 */
@UtilityClass
public class TaskDependencyChecker {

    /**
     * 检查任务配置中是否存在循环依赖
     *
     * @param arrangeRule 任务配置
     * @return true表示存在循环依赖，false表示不存在
     */
    public static boolean hasCircularDependency(List<List<String>> arrangeRule) {
        Map<String, List<String>> graph = buildGraph(arrangeRule);
        return !topologicalSort(graph);
    }

    /**
     * 构建任务依赖图
     *
     * @param arrangeRule 任务配置
     * @return 任务依赖图
     */
    private static Map<String, List<String>> buildGraph(List<List<String>> arrangeRule) {
        Map<String, List<String>> graph = new HashMap<>();

        // 处理前置任务
        List<String> preTasks = arrangeRule.get(0);
        for (String task : preTasks) {
            graph.putIfAbsent(task, new ArrayList<>());
        }

        // 处理后置任务
        List<String> postTasks = arrangeRule.get(arrangeRule.size() - 1);
        for (String task : postTasks) {
            graph.putIfAbsent(task, new ArrayList<>());
        }

        // 处理中间的普通任务
        for (int i = 1; i < arrangeRule.size() - 1; i++) {
            List<String> arrange = arrangeRule.get(i);
            for (String arrangeLine : arrange) {
                if (arrangeLine.contains(":")) {
                    String[] parts = arrangeLine.split(":");
                    String[] fromTasks = parts[0].split(",");
                    String[] toTasks = parts[1].split(",");
                    for (String fromTask : fromTasks) {
                        graph.putIfAbsent(fromTask, new ArrayList<>());
                        for (String toTask : toTasks) {
                            graph.putIfAbsent(toTask, new ArrayList<>()); // 确保toTask也被初始化
                            graph.get(fromTask).add(toTask);
                        }
                    }
                } else {
                    String[] tasks = arrangeLine.split(",");
                    for (String task : tasks) {
                        graph.putIfAbsent(task, new ArrayList<>());
                    }
                }
            }
        }

        // 将前置任务指向所有中间任务的起始任务
        for (String preTask : preTasks) {
            for (int i = 1; i < arrangeRule.size() - 1; i++) {
                List<String> arrange = arrangeRule.get(i);
                for (String arrangeLine : arrange) {
                    String[] tasks = arrangeLine.split(":")[0].split(",");
                    for (String task : tasks) {
                        graph.putIfAbsent(task, new ArrayList<>()); // 确保task也被初始化
                        graph.get(preTask).add(task);
                    }
                }
            }
        }

        // 将所有中间任务的终止任务指向后置任务
        for (int i = 1; i < arrangeRule.size() - 1; i++) {
            List<String> arrange = arrangeRule.get(i);
            for (String arrangeLine : arrange) {
                String[] tasks = arrangeLine.split(":").length > 1 ? arrangeLine.split(":")[1].split(",") : arrangeLine.split(",");
                for (String task : tasks) {
                    graph.putIfAbsent(task, new ArrayList<>()); // 确保task也被初始化
                    for (String postTask : postTasks) {
                        graph.get(task).add(postTask);
                    }
                }
            }
        }

        return graph;
    }

    /**
     * 拓扑排序检测环
     *
     * @param graph 任务依赖图
     * @return true表示没有环，false表示有环
     */
    private static boolean topologicalSort(Map<String, List<String>> graph) {
        Map<String, Integer> inDegree = new HashMap<>();
        for (String node : graph.keySet()) {
            inDegree.put(node, 0);
        }
        for (List<String> neighbors : graph.values()) {
            for (String neighbor : neighbors) {
                inDegree.put(neighbor, inDegree.getOrDefault(neighbor, 0) + 1);
            }
        }

        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        int visitedNodes = 0;
        while (!queue.isEmpty()) {
            String node = queue.poll();
            visitedNodes++;
            if (graph.containsKey(node)) {
                for (String neighbor : graph.get(node)) {
                    inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                    if (inDegree.get(neighbor) == 0) {
                        queue.add(neighbor);
                    }
                }
            }
        }

        return visitedNodes == inDegree.size();
    }

    public static void main(String[] args) {
        // 示例任务配置
        List<List<String>> arrangeRule = Arrays.asList(
                Arrays.asList("1,2"), // 前置任务
                Arrays.asList(
                        "3,4",   // 任务3和任务4并行执行
                        "3:5",   // 任务3执行完了之后执行任务5
                        "4:6",   // 任务4执行完了之后执行任务6
                        "5,6:7"  // 任务5和任务6都执行完毕之后，再执行任务7
                ),
                Arrays.asList("1000") // 后置任务
        );

        boolean hasCycle = TaskDependencyChecker.hasCircularDependency(arrangeRule);
        if (hasCycle) {
            System.out.println("任务配置1中存在循环依赖");
        } else {
            System.out.println("任务配置1中不存在循环依赖");
        }


        // 示例任务配置
        List<List<String>> arrangeRule2 = Arrays.asList(
                Arrays.asList("1,2"), // 前置任务
                Arrays.asList(
                        "3,4",   // 任务3和任务4并行执行
                        "3:5",   // 任务3执行完了之后执行任务5
                        "4:6",   // 任务4执行完了之后执行任务6
                        "5,6:7",  // 任务5和任务6都执行完毕之后，再执行任务7
                        "7:3"  // 任务7执行完了之后执行任务3
                ),
                Arrays.asList("1000") // 后置任务
        );

        boolean hasCycle2 = TaskDependencyChecker.hasCircularDependency(arrangeRule2);
        if (hasCycle2) {
            System.out.println("任务配置2中存在循环依赖");
        } else {
            System.out.println("任务配置2中不存在循环依赖");
        }

    }
}

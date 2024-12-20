package com.oneinstep.haidu.parser;

import com.oneinstep.haidu.config.TaskDetail;
import com.oneinstep.haidu.core.AbstractTask;
import com.oneinstep.haidu.exception.HaiduException;
import com.oneinstep.haidu.exception.IllegalTaskConfigException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 任务表达式解析器
 * 规则：任务表达式由并行任务和依赖任务组成，用冒号分隔，如：["task1,task2:task3"]
 * 表示task1和task2并行执行，task3在task1和task2执行完后执行
 * <p>
 * task1,task2 执行完执行 task3 和 task4
 * 可以这样一行定义 ["task1,task2:task3,task4"]
 * 也可以这样两行定义 ["task1,task2:task3", "task1,task2:task4"]
 */
@Slf4j
public class ExpressionParser {

    private ExpressionParser() {
    }

    /**
     * 解析任务表达式，生成任务图
     *
     * @param expressions     任务表达式列表
     * @param taskInstanceMap 任务实例缓存
     * @param taskDetailMap   任务详情映射
     * @return 解析后的任务图
     */
    public static TaskGraph parseExpressions(List<String> expressions, Map<String, AbstractTask<?>> taskInstanceMap,
                                             Map<String, TaskDetail> taskDetailMap) {
        TaskGraph graph = new TaskGraph();

        for (String expression : expressions) {
            // 解析任务表达式，获取并行任务和依赖任务
            String[] parts = expression.split(":");
            String[] parallelTasks = parts[0].split(",");

            // 处理并行任务的前置任务
            for (String taskId : parallelTasks) {
                // 获取并存储任务实例
                AbstractTask<?> task = getAndCacheTask(taskId, taskInstanceMap, taskDetailMap);
                // 将任务添加到任务图中
                graph.addTask(taskId, task);
            }

            // 处理依赖任务
            if (parts.length > 1) {
                String[] dependentTaskIds = parts[1].split(",");
                // 遍历依赖任务
                for (String dependentTaskId : dependentTaskIds) {
                    // 获取并存储依赖任务实例
                    AbstractTask<?> task = getAndCacheTask(dependentTaskId, taskInstanceMap, taskDetailMap);
                    // 将依赖任务添加到任务图中
                    graph.addTask(dependentTaskId, task);

                    // 遍历并行任务
                    for (String taskId : parallelTasks) {
                        // 添加任务依赖关系
                        graph.addDependency(dependentTaskId, taskId);
                    }
                }
            }
        }

        // 检测循环依赖
        if (hasCycle(graph)) {
            throw new IllegalTaskConfigException("检测到循环依赖！");
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
    public static AbstractTask<?> getAndCacheTask(String taskId, Map<String, AbstractTask<?>> taskInstanceMap,
                                                  Map<String, TaskDetail> taskDetailMap) {
        // 从缓存中获取任务实例
        AbstractTask<?> task = taskInstanceMap.get(taskId);
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
                throw new HaiduException("无效taskClassName");
            }
            // 通过反射创建任务实例
            task = (AbstractTask<?>) Class.forName(taskClassName).getDeclaredConstructor().newInstance();
            task.setTaskId(taskId);
            task.setRetryTimes(retryTimes);
            task.setTimeout(timeout);
            task.setParams(params);

            // 将任务实例存储到缓存中
            taskInstanceMap.put(taskId, task);
            return task;
        } catch (Exception e) {
            log.error("Instantiation Exception during taskId={}, taskName={}", taskId, taskClassName, e);
            throw new HaiduException(e);
        }
    }

    /**
     * 检测任务图中是否存在循环依赖
     *
     * @param graph 任务图
     * @return 如果存在循环依赖，则返回 true；否则返回 false
     */
    private static boolean hasCycle(TaskGraph graph) {
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();

        // 遍历所有任务ID，检测循环依赖
        for (String taskId : graph.getAllTaskIds()) {
            if (hasCycleUtil(taskId, visited, recStack, graph)) {
                return true;
            }
        }

        return false;
    }

    /**
     * DFS辅助方法，用于检测循环依赖
     *
     * @param taskId   当前任务ID
     * @param visited  已访问的任务ID集合
     * @param recStack 递归栈中的任务ID集合
     * @param graph    任务图
     * @return 如果存在循环依赖，则返回 true；否则返回 false
     */
    private static boolean hasCycleUtil(String taskId, Set<String> visited, Set<String> recStack, TaskGraph graph) {

        // 如果任务ID已经在递归栈中，则存在循环依赖
        if (recStack.contains(taskId)) {
            return true;
        }

        // 如果任务ID已经访问过，则跳过
        if (visited.contains(taskId)) {
            return false;
        }

        // 将任务ID添加到已访问和递归栈中
        visited.add(taskId);
        recStack.add(taskId);

        // 递归检测依赖任务
        for (String dependentTaskId : graph.getDependencies(taskId)) {
            // 如果依赖任务存在循环依赖，则返回 true
            if (hasCycleUtil(dependentTaskId, visited, recStack, graph)) {
                return true;
            }
        }

        // 从递归栈中移除任务ID
        recStack.remove(taskId);
        // 返回 false，表示不存在循环依赖
        return false;
    }
}

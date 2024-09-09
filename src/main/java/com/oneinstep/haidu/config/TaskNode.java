package com.oneinstep.haidu.config;

import com.oneinstep.haidu.core.AbstractTask;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public class TaskNode {

    String taskId;
    AbstractTask task;
    List<TaskNode> dependencies = new ArrayList<>();

    TaskNode(String taskId, AbstractTask task) {
        this.taskId = taskId;
        this.task = task;
    }

}

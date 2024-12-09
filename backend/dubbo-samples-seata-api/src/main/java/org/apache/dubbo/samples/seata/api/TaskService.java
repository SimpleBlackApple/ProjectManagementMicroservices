package org.apache.dubbo.samples.seata.api;

import org.apache.dubbo.samples.seata.api.dto.*;
import java.util.List;

public interface TaskService {
    // Sprint 相关方法
    SprintDTO getSprintById(Integer memberId, Integer projectId, Integer sprintId);
    List<SprintDTO> getProjectSprints(Integer memberId, Integer projectId);
    SprintDTO createSprint(Integer memberId, Integer projectId, SprintCreateBody createBody);
    SprintDTO updateSprint(Integer memberId, Integer projectId, Integer sprintId, SprintUpdateBody updateBody);
    void deleteSprint(Integer memberId, Integer projectId, Integer sprintId);

    // Task 相关方法
    TaskDTO getTaskById(Integer memberId, Integer taskId);
    List<TaskDTO> getProjectTasks(Integer memberId, Integer projectId);
    List<TaskDTO> getSprintTasks(Integer memberId, Integer projectId, Integer sprintId);
    TaskDTO createTask(Integer memberId, Integer projectId, TaskCreateBody createBody);
    TaskDTO updateTask(Integer memberId, Integer taskId, TaskUpdateBody updateBody);
    void deleteTask(Integer memberId, Integer taskId);

    // 添加删除项目相关的所有 sprints 和 tasks 的方法
    void deleteProjectRelatedItems(Integer projectId);
} 
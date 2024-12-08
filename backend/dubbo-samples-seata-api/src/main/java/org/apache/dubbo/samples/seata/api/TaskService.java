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
    TaskDTO getTaskById(Integer taskId);
    List<TaskDTO> getProjectTasks(Integer projectId);
    List<TaskDTO> getSprintTasks(Integer sprintId);
    TaskDTO createTask(TaskCreateBody createBody);
    TaskDTO updateTask(Integer taskId, TaskUpdateBody updateBody);
    void deleteTask(Integer taskId);
} 
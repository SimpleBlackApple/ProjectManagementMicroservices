package org.apache.dubbo.samples.seata.api;

import org.apache.dubbo.samples.seata.api.dto.*;
import java.util.List;

public interface TaskService {
    // Sprint 相关方法
    SprintDTO getSprintById(Integer sprintId);
    List<SprintDTO> getProjectSprints(Integer projectId);
    SprintDTO createSprint(SprintCreateBody createBody);
    SprintDTO updateSprint(Integer sprintId, SprintUpdateBody updateBody);
    void deleteSprint(Integer sprintId);

    // Task 相关方法
    TaskDTO getTaskById(Integer taskId);
    List<TaskDTO> getProjectTasks(Integer projectId);
    List<TaskDTO> getSprintTasks(Integer sprintId);
    TaskDTO createTask(TaskCreateBody createBody);
    TaskDTO updateTask(Integer taskId, TaskUpdateBody updateBody);
    void deleteTask(Integer taskId);

    // Member 相关方法
    void addProjectMember(Integer projectId, Integer memberId);
    void removeProjectMember(Integer projectId, Integer memberId);
    List<MemberDTO> getProjectMembers(Integer projectId);
} 
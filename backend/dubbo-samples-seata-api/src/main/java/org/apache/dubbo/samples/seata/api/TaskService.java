package org.apache.dubbo.samples.seata.api;

import org.apache.dubbo.samples.seata.api.dto.*;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;

public interface TaskService {
    // Sprint 相关方法
    SprintDTO getSprintById(UserDetails user, Integer sprintId);
    List<SprintDTO> getProjectSprints(UserDetails user, Integer projectId);
    SprintDTO createSprint(Integer userId, Integer projectId, SprintCreateBody createBody);
    SprintDTO updateSprint(UserDetails user, Integer sprintId, SprintUpdateBody updateBody);
    void deleteSprint(UserDetails user, Integer sprintId);

    // Task 相关方法
    TaskDTO getTaskById(UserDetails user, Integer taskId);
    List<TaskDTO> getProjectTasks(UserDetails user, Integer projectId);
    List<TaskDTO> getSprintTasks(UserDetails user, Integer projectId, Integer sprintId);
    TaskDTO createTask(UserDetails user, Integer projectId, TaskCreateBody createBody);
    TaskDTO updateTask(UserDetails user, Integer taskId, TaskUpdateBody updateBody);
    void deleteTask(UserDetails user, Integer taskId);

    // 添加删除项目相关的所有 sprints ��� tasks 的方法
    void deleteProjectRelatedItems(Integer projectId);

    // 添加同步用户方法
    void syncNewUser(Integer userId, String email);
} 
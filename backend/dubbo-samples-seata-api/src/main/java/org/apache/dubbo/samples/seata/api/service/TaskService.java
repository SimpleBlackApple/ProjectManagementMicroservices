package org.apache.dubbo.samples.seata.api.service;

import org.apache.dubbo.samples.seata.api.dto.*;

import java.util.List;

public interface TaskService {
    // Sprint 相关方法
    SprintDTO getSprintById(String email, Integer sprintId);
    List<SprintDTO> getProjectSprints(String email, Integer projectId);
    SprintDTO createSprint(String email, Integer projectId, SprintCreateBody createBody);
    SprintDTO updateSprint(String email, Integer sprintId, SprintUpdateBody updateBody);
    void deleteSprint(String email, Integer sprintId);

    // Task 相关方法
    TaskDTO getTaskById(String email, Integer taskId);
    List<TaskDTO> getProjectTasks(String email, Integer projectId);
    List<TaskDTO> getSprintTasks(String email, Integer projectId, Integer sprintId);
    TaskDTO createTask(String email, Integer projectId, TaskCreateBody createBody);
    TaskDTO updateTask(String email, Integer taskId, TaskUpdateBody updateBody);
    void deleteTask(String email, Integer taskId);

    // 添加删除项目相关的所有 sprints  tasks 的方法
    void deleteProjectRelatedItems(Integer projectId);

    // 添加同步用户方法
    void syncNewUser(Integer userId, String name, String email, String password);

    // 添加删除用户相关数据的方法
    void removeUserData(String email);
} 
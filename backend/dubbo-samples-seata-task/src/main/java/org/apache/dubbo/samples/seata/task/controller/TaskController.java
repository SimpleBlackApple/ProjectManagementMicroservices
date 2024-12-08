package org.apache.dubbo.samples.seata.task.controller;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.samples.seata.api.TaskService;  
import org.apache.dubbo.samples.seata.api.dto.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    private final TaskService taskService;
    
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    
    // Task 相关接口
    @GetMapping("/projects/{projectId}/members/{memberId}/tasks/{taskId}")
    public TaskDTO getTask(
        @PathVariable Integer memberId,
        @PathVariable Integer taskId
    ) {
        return taskService.getTaskById(memberId, taskId);
    }

    @GetMapping("/projects/{projectId}/members/{memberId}/tasks")
    public List<TaskDTO> getProjectTasks(
        @PathVariable Integer memberId,
        @PathVariable Integer projectId
    ) {
        return taskService.getProjectTasks(memberId, projectId);
    }
    @GetMapping("/projects/{projectId}/members/{memberId}/sprints/{sprintId}/tasks")
    public List<TaskDTO> getSprintTasks(
            @PathVariable Integer memberId,
            @PathVariable Integer projectId,
            @PathVariable Integer sprintId
    ) {
        return taskService.getSprintTasks(memberId, projectId, sprintId);
    }

    @PostMapping("/projects/{projectId}/members/{memberId}/tasks")
    public TaskDTO createTask(
        @PathVariable Integer memberId,
        @PathVariable Integer projectId,
        @RequestBody TaskCreateBody createBody
    ) {
        return taskService.createTask(memberId, projectId, createBody);
    }

    @PutMapping("/projects/{projectId}/members/{memberId}/tasks/{taskId}")
    public TaskDTO updateTask(
        @PathVariable Integer memberId,
        @PathVariable Integer taskId,
        @RequestBody TaskUpdateBody updateBody
    ) {
        return taskService.updateTask(memberId, taskId, updateBody);
    }

    @DeleteMapping("/projects/{projectId}/members/{memberId}/tasks/{taskId}")
    public void deleteTask(
        @PathVariable Integer memberId,
        @PathVariable Integer taskId
    ) {
        taskService.deleteTask(memberId, taskId);
    }

    // Sprint 相关接口
    @GetMapping("/projects/{projectId}/members/{memberId}/sprints/{sprintId}")
    public SprintDTO getSprint(
        @PathVariable Integer memberId,
        @PathVariable Integer projectId,
        @PathVariable Integer sprintId
    ) {
        return taskService.getSprintById(memberId, projectId, sprintId);
    }

    @GetMapping("/projects/{projectId}/members/{memberId}/sprints")
    public List<SprintDTO> getProjectSprints(
        @PathVariable Integer memberId,
        @PathVariable Integer projectId
    ) {
        return taskService.getProjectSprints(memberId, projectId);
    }

    @PostMapping("/projects/{projectId}/members/{memberId}/sprints")
    public SprintDTO createSprint(
        @PathVariable Integer memberId,
        @PathVariable Integer projectId,
        @RequestBody SprintCreateBody createBody
    ) {
        return taskService.createSprint(memberId, projectId, createBody);
    }

    @PutMapping("/projects/{projectId}/members/{memberId}/sprints/{sprintId}")
    public SprintDTO updateSprint(
        @PathVariable Integer memberId,
        @PathVariable Integer projectId,
        @PathVariable Integer sprintId,
        @RequestBody SprintUpdateBody updateBody
    ) {
        return taskService.updateSprint(memberId, projectId, sprintId, updateBody);
    }

    @DeleteMapping("/projects/{projectId}/members/{memberId}/sprints/{sprintId}")
    public void deleteSprint(
        @PathVariable Integer memberId,
        @PathVariable Integer projectId,
        @PathVariable Integer sprintId
    ) {
        taskService.deleteSprint(memberId, projectId, sprintId);
    }


}
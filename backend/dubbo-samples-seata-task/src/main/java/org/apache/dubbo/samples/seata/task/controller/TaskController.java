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

    // Task 相关接口
    @GetMapping("/projects/{projectId}/tasks")
    public List<TaskDTO> getProjectTasks(@PathVariable Integer projectId) {
        return taskService.getProjectTasks(projectId);
    }

    @GetMapping("/sprints/{sprintId}/tasks")
    public List<TaskDTO> getSprintTasks(@PathVariable Integer sprintId) {
        return taskService.getSprintTasks(sprintId);
    }

    @PostMapping
    public TaskDTO createTask(@RequestBody TaskCreateBody createBody) {
        return taskService.createTask(createBody);
    }

    @GetMapping("/{taskId}")
    public TaskDTO getTask(@PathVariable Integer taskId) {
        return taskService.getTaskById(taskId);
    }

    @PutMapping("/{taskId}")
    public TaskDTO updateTask(@PathVariable Integer taskId, @RequestBody TaskUpdateBody updateBody) {
        return taskService.updateTask(taskId, updateBody);
    }

    @DeleteMapping("/{taskId}")
    public void deleteTask(@PathVariable Integer taskId) {
        taskService.deleteTask(taskId);
    }
}
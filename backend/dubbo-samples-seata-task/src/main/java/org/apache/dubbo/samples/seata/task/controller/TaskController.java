package org.apache.dubbo.samples.seata.task.controller;

import org.apache.dubbo.samples.seata.api.TaskService;  
import org.apache.dubbo.samples.seata.api.dto.*;
import org.apache.dubbo.samples.seata.task.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class TaskController {
    
    private final TaskService taskService;
    
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    private UserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserDetails) authentication.getPrincipal();
    }
    
    // Task 相关接口
    @GetMapping("/tasks/{taskId}")
    public TaskDTO getTask(
        @PathVariable Integer taskId
    ) {
        return taskService.getTaskById(getCurrentUser(), taskId);
    }

    @PutMapping("/tasks/{taskId}")
    public TaskDTO updateTask(
        @PathVariable Integer taskId,
        @RequestBody TaskUpdateBody updateBody
    ) {
        return taskService.updateTask(getCurrentUser(), taskId, updateBody);
    }

    @DeleteMapping("/tasks/{taskId}")
    public void deleteTask(
        @PathVariable Integer taskId
    ) {
        taskService.deleteTask(getCurrentUser(), taskId);
    }

    // Sprint 相关接口
    @GetMapping("/sprints/{sprintId}")
    public SprintDTO getSprint(
        @PathVariable Integer sprintId
    ) {
        return taskService.getSprintById(getCurrentUser(), sprintId);
    }

    @PutMapping("/sprints/{sprintId}")
    public SprintDTO updateSprint(
        @PathVariable Integer sprintId,
        @RequestBody SprintUpdateBody updateBody
    ) {
        return taskService.updateSprint(getCurrentUser(), sprintId, updateBody);
    }

    @DeleteMapping("/sprints/{sprintId}")
    public void deleteSprint(
        @PathVariable Integer sprintId
    ) {
        taskService.deleteSprint(getCurrentUser(), sprintId);
    }

    // 需要 projectId 的操作
    @GetMapping("/projects/{projectId}/tasks")
    public List<TaskDTO> getProjectTasks(
        @PathVariable Integer projectId
    ) {
        return taskService.getProjectTasks(getCurrentUser(), projectId);
    }

    @PostMapping("/projects/{projectId}/tasks")
    public TaskDTO createTask(
        @PathVariable Integer projectId,
        @RequestBody TaskCreateBody createBody
    ) {
        return taskService.createTask(getCurrentUser(), projectId, createBody);
    }

    @GetMapping("/projects/{projectId}/sprints/{sprintId}/tasks")
    public List<TaskDTO> getSprintTasks(
        @PathVariable Integer projectId,
        @PathVariable Integer sprintId
    ) {
        return taskService.getSprintTasks(getCurrentUser(), projectId, sprintId);
    }

    // 需要 projectId 的 Sprint 操作
    @GetMapping("/projects/{projectId}/sprints")
    public List<SprintDTO> getProjectSprints(
        @PathVariable Integer projectId
    ) {
        return taskService.getProjectSprints(getCurrentUser(), projectId);
    }

    @PostMapping("/projects/{projectId}/sprints")
    public SprintDTO createSprint(
        @PathVariable Integer projectId,
        @RequestBody SprintCreateBody createBody
    ) {
        User user = (User) getCurrentUser();
        return taskService.createSprint(user.getId(), projectId, createBody);
    }
}
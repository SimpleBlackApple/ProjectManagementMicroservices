package org.apache.dubbo.samples.seata.task.controller;

import org.apache.dubbo.samples.seata.api.service.TaskService;
import org.apache.dubbo.samples.seata.api.dto.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class TaskController {
    
    private final TaskService taskService;
    
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetails) authentication.getPrincipal()).getUsername();
    }
    
    // Task 相关接口
    @GetMapping("/tasks/{taskId}")
    public TaskDTO getTask(
        @PathVariable Integer taskId
    ) {
        return taskService.getTaskById(getCurrentUserEmail(), taskId);
    }

    @PutMapping("/tasks/{taskId}")
    public TaskDTO updateTask(
        @PathVariable Integer taskId,
        @RequestBody TaskUpdateBody updateBody
    ) {
        return taskService.updateTask(getCurrentUserEmail(), taskId, updateBody);
    }

    @DeleteMapping("/tasks/{taskId}")
    public void deleteTask(
        @PathVariable Integer taskId
    ) {
        taskService.deleteTask(getCurrentUserEmail(), taskId);
    }

    // Sprint 相关接口
    @GetMapping("/sprints/{sprintId}")
    public SprintDTO getSprint(
        @PathVariable Integer sprintId
    ) {
        return taskService.getSprintById(getCurrentUserEmail(), sprintId);
    }

    @PutMapping("/sprints/{sprintId}")
    public SprintDTO updateSprint(
        @PathVariable Integer sprintId,
        @RequestBody SprintUpdateBody updateBody
    ) {
        return taskService.updateSprint(getCurrentUserEmail(), sprintId, updateBody);
    }

    @DeleteMapping("/sprints/{sprintId}")
    public void deleteSprint(
        @PathVariable Integer sprintId
    ) {
        taskService.deleteSprint(getCurrentUserEmail(), sprintId);
    }

    // 需要 projectId 的操作
    @GetMapping("/projects/{projectId}/tasks")
    public List<TaskDTO> getProjectTasks(
        @PathVariable Integer projectId
    ) {
        return taskService.getProjectTasks(getCurrentUserEmail(), projectId);
    }

    @PostMapping("/projects/{projectId}/tasks")
    public TaskDTO createTask(
        @PathVariable Integer projectId,
        @RequestBody TaskCreateBody createBody
    ) {
        return taskService.createTask(getCurrentUserEmail(), projectId, createBody);
    }

    @GetMapping("/projects/{projectId}/sprints/{sprintId}/tasks")
    public List<TaskDTO> getSprintTasks(
        @PathVariable Integer projectId,
        @PathVariable Integer sprintId
    ) {
        return taskService.getSprintTasks(getCurrentUserEmail(), projectId, sprintId);
    }

    // 需要 projectId 的 Sprint 操作
    @GetMapping("/projects/{projectId}/sprints")
    public List<SprintDTO> getProjectSprints(
        @PathVariable Integer projectId
    ) {
        return taskService.getProjectSprints(getCurrentUserEmail(), projectId);
    }

    @PostMapping("/projects/{projectId}/sprints")
    public SprintDTO createSprint(
        @PathVariable Integer projectId,
        @RequestBody SprintCreateBody createBody
    ) {
        return taskService.createSprint(getCurrentUserEmail(), projectId, createBody);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        if (e.getMessage().contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
        }
        if (e.getMessage().contains("Access denied")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(e.getMessage());
        }
        if (e.getMessage().contains("Only task assignee")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(e.getMessage());
    }
}
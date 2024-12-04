package org.apache.dubbo.samples.seata.task.controller;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.samples.seata.api.TaskService;  
import org.apache.dubbo.samples.seata.api.dto.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    @DubboReference(check = false)
    private TaskService taskService;
    
//    public TaskController(TaskService taskService) {
//        this.taskService = taskService;
//    }
    
    // Sprint 相关接口
    @GetMapping("/sprints/{sprintId}")
    public SprintDTO getSprint(@PathVariable Integer sprintId) {
        return taskService.getSprintById(sprintId);
    }

    @GetMapping("/projects/{projectId}/sprints")
    public List<SprintDTO> getProjectSprints(@PathVariable Integer projectId) {
        return taskService.getProjectSprints(projectId);
    }

    @PostMapping("/sprints")
    public SprintDTO createSprint(@RequestBody SprintCreateBody createBody) {
        return taskService.createSprint(createBody);
    }

    @PutMapping("/sprints/{sprintId}")
    public SprintDTO updateSprint(@PathVariable Integer sprintId, @RequestBody SprintUpdateBody updateBody) {
        return taskService.updateSprint(sprintId, updateBody);
    }

    @DeleteMapping("/sprints/{sprintId}")
    public void deleteSprint(@PathVariable Integer sprintId) {
        taskService.deleteSprint(sprintId);
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

    // Member 相关接口
    @PostMapping("/projects/{projectId}/members/{memberId}")
    public void addProjectMember(@PathVariable Integer projectId, @PathVariable Integer memberId) {
        taskService.addProjectMember(projectId, memberId);
    }

    @DeleteMapping("/projects/{projectId}/members/{memberId}")
    public void removeProjectMember(@PathVariable Integer projectId, @PathVariable Integer memberId) {
        taskService.removeProjectMember(projectId, memberId);
    }

    @GetMapping("/projects/{projectId}/members")
    public List<MemberDTO> getProjectMembers(@PathVariable Integer projectId) {
        return taskService.getProjectMembers(projectId);
    }
}
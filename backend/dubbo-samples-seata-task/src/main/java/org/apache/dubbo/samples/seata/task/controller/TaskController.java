package org.apache.dubbo.samples.seata.task.controller;

import org.apache.dubbo.samples.seata.api.dto.ProjectCreateBody;
import org.apache.dubbo.samples.seata.api.dto.ProjectDTO;
import org.apache.dubbo.samples.seata.task.service.TaskService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    private final TaskService taskService;
    
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    
    @PostMapping("/projects")
    public ProjectDTO createProject(@RequestBody ProjectCreateBody createBody) {
        return taskService.createProjectWithOwner(createBody);
    }
    
    @PutMapping("/projects/{projectId}/owner/{newOwnerId}")
    public ProjectDTO updateProjectOwner(@PathVariable Integer projectId, @PathVariable Integer newOwnerId) {
        return taskService.updateProjectOwner(projectId, newOwnerId);
    }
    
    @GetMapping("/test")
    public String test() {
        return "Task Controller is working!";
    }
} 
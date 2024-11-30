package org.apache.dubbo.samples.seata.project.controller;

import org.apache.dubbo.samples.seata.api.ProjectService;
import org.apache.dubbo.samples.seata.api.dto.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/{projectId}")
    public ProjectDTO getProject(@PathVariable Integer projectId) {
        return projectService.getProjectById(projectId);
    }

    @GetMapping
    public List<ProjectDTO> getAllProjects() {
        return projectService.getAllProjects();
    }

    @PostMapping
    public ProjectDTO createProject(@RequestBody ProjectCreateBody createBody) {
        return projectService.createProject(createBody);
    }

    @PutMapping("/{projectId}")
    public ProjectDTO updateProject(@PathVariable Integer projectId, @RequestBody ProjectUpdateBody updateBody) {
        return projectService.updateProject(projectId, updateBody);
    }

    @DeleteMapping("/{projectId}")
    public void deleteProject(@PathVariable Integer projectId) {
        projectService.deleteProject(projectId);
    }

    @GetMapping("/{projectId}/sprints")
    public List<SprintDTO> getProjectSprints(@PathVariable Integer projectId) {
        return projectService.getProjectSprints(projectId);
    }

    @PostMapping("/sprints")
    public SprintDTO createSprint(@RequestBody SprintCreateBody createBody) {
        return projectService.createSprint(createBody);
    }

    @GetMapping("/sprints/{sprintId}")
    public SprintDTO getSprint(@PathVariable Integer sprintId) {
        return projectService.getSprintById(sprintId);
    }

    @PutMapping("/sprints/{sprintId}")
    public SprintDTO updateSprint(@PathVariable Integer sprintId, @RequestBody SprintUpdateBody updateBody) {
        return projectService.updateSprint(sprintId, updateBody);
    }

    @DeleteMapping("/sprints/{sprintId}")
    public void deleteSprint(@PathVariable Integer sprintId) {
        projectService.deleteSprint(sprintId);
    }

    @GetMapping("/{projectId}/tasks")
    public List<TaskDTO> getProjectTasks(@PathVariable Integer projectId) {
        return projectService.getProjectTasks(projectId);
    }

    @GetMapping("/sprints/{sprintId}/tasks")
    public List<TaskDTO> getSprintTasks(@PathVariable Integer sprintId) {
        return projectService.getSprintTasks(sprintId);
    }

    @PostMapping("/tasks")
    public TaskDTO createTask(@RequestBody TaskCreateBody createBody) {
        return projectService.createTask(createBody);
    }

    @GetMapping("/tasks/{taskId}")
    public TaskDTO getTask(@PathVariable Integer taskId) {
        return projectService.getTaskById(taskId);
    }

    @PutMapping("/tasks/{taskId}")
    public TaskDTO updateTask(@PathVariable Integer taskId, @RequestBody TaskUpdateBody updateBody) {
        return projectService.updateTask(taskId, updateBody);
    }

    @DeleteMapping("/tasks/{taskId}")
    public void deleteTask(@PathVariable Integer taskId) {
        projectService.deleteTask(taskId);
    }

    @PostMapping("/{projectId}/members/{memberId}")
    public void addProjectMember(@PathVariable Integer projectId, @PathVariable Integer memberId) {
        projectService.addProjectMember(projectId, memberId);
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    public void removeProjectMember(@PathVariable Integer projectId, @PathVariable Integer memberId) {
        projectService.removeProjectMember(projectId, memberId);
    }

    @GetMapping("/{projectId}/members")
    public List<MemberDTO> getProjectMembers(@PathVariable Integer projectId) {
        return projectService.getProjectMembers(projectId);
    }
} 
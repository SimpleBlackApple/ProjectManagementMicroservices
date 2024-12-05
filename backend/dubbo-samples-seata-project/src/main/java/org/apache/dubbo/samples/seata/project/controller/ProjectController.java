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

    @PutMapping("/{userId}/{projectId}")
    public ProjectDTO updateProject(
        @PathVariable Integer userId,
        @PathVariable Integer projectId, 
        @RequestBody ProjectUpdateBody updateBody
    ) {
        return projectService.updateProject(userId, projectId, updateBody);
    }

    @DeleteMapping("/{projectId}")
    public void deleteProject(@PathVariable Integer projectId) {
        projectService.deleteProject(projectId);
    }

} 
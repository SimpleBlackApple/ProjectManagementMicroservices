package org.apache.dubbo.samples.seata.backlog.controller;

import org.apache.dubbo.samples.seata.api.dto.ProjectCreateBody;
import org.apache.dubbo.samples.seata.api.dto.ProjectDTO;
import org.apache.dubbo.samples.seata.backlog.service.BacklogService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backlog")
public class BacklogController {
    
    private final BacklogService backlogService;
    
    public BacklogController(BacklogService backlogService) {
        this.backlogService = backlogService;
    }
    
    @PostMapping("/projects")
    public ProjectDTO createProject(@RequestBody ProjectCreateBody createBody) {
        return backlogService.createProjectWithOwner(createBody);
    }
    
    @PutMapping("/projects/{projectId}/owner/{newOwnerId}")
    public ProjectDTO updateProjectOwner(@PathVariable Integer projectId, @PathVariable Integer newOwnerId) {
        return backlogService.updateProjectOwner(projectId, newOwnerId);
    }
} 
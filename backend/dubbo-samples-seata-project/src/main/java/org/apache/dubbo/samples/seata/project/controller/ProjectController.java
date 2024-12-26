package org.apache.dubbo.samples.seata.project.controller;

import org.apache.dubbo.samples.seata.api.service.ProjectService;
import org.apache.dubbo.samples.seata.api.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetails) authentication.getPrincipal()).getUsername();
    }

    @GetMapping("/owned")
    public List<ProjectDTO> getOwnedProjects() {
        return projectService.getProjectByOwner(getCurrentUserEmail());
    }

    @GetMapping()
    public List<ProjectDTO> getAllProjects() {
        return projectService.getAllProjects(getCurrentUserEmail());
    }

    @GetMapping("/{projectId}")
    public ProjectDTO getProject(@PathVariable Integer projectId) {
        return projectService.getProject(getCurrentUserEmail(), projectId);
    }

    @PostMapping
    public ProjectDTO createProject(@RequestBody ProjectCreateBody createBody) {
        return projectService.createProject(getCurrentUserEmail(), createBody);
    }

    @PutMapping("/{projectId}")
    public ProjectDTO updateProject(
        @PathVariable Integer projectId,
        @RequestBody ProjectUpdateBody updateBody
    ) {
        return projectService.updateProject(getCurrentUserEmail(), projectId, updateBody);
    }

    @PostMapping("/{projectId}/members/{newUserId}")
    public ProjectDTO addMember(
        @PathVariable Integer projectId,
        @PathVariable Integer newUserId
    ) {
        return projectService.addMember(getCurrentUserEmail(), projectId, newUserId);
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    public void removeMember(
        @PathVariable Integer projectId,
        @PathVariable Integer memberId
    ) {
        projectService.removeMember(getCurrentUserEmail(), projectId, memberId);
    }

    @GetMapping("/{projectId}/members")
    public List<MemberDTO> getProjectMembers(@PathVariable Integer projectId) {
        return projectService.getProjectMembers(getCurrentUserEmail(), projectId);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable Integer projectId) {
        try {
            projectService.deleteProject(getCurrentUserEmail(), projectId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Project not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
            }
            if (e.getMessage().contains("Only project owner")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }

    @PostMapping("/{projectId}/transfer-ownership/{newOwnerId}")
    public ProjectDTO transferOwnership(
        @PathVariable Integer projectId,
        @PathVariable Integer newOwnerId
    ) {
        return projectService.transferOwnership(getCurrentUserEmail(), projectId, newOwnerId);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        if (e.getMessage().contains("Project not found") || 
            e.getMessage().contains("User not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
        }
        if (e.getMessage().contains("Access denied")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(e.getMessage());
        }
        if (e.getMessage().contains("User is already a member")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(e.getMessage());
        }
        if (e.getMessage().contains("Only project owner")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(e.getMessage());
    }
} 
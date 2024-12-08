package org.apache.dubbo.samples.seata.project.controller;

import org.apache.dubbo.samples.seata.api.ProjectService;
import org.apache.dubbo.samples.seata.api.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/member/{memberId}")
    public List<ProjectDTO> getProjectByMember(@PathVariable Integer memberId) {
        // 获取该成员作为owner的所有项目
        return projectService.getProjectByOwnerId(memberId);
    }

    @GetMapping()
    public List<ProjectDTO> getAllProjects() {
        // 获取该成员参与的所有项目（包括作为owner和普通成员的）
        return projectService.getAllProjects();
    }

    @GetMapping("/{memberId}/{projectId}")
    public ProjectDTO getProject(
            @PathVariable Integer memberId,
            @PathVariable Integer projectId
    ) {
        return projectService.getProject(memberId, projectId);
    }

    @PostMapping("/{ownerId}")
    public ProjectDTO createProject(
        @PathVariable Integer ownerId,
        @RequestBody ProjectCreateBody createBody
    ) {
        return projectService.createProject(ownerId, createBody);
    }

    @PutMapping("/{memberId}/{projectId}")
    public ProjectDTO updateProject(
        @PathVariable Integer memberId,
        @PathVariable Integer projectId,
        @RequestBody ProjectUpdateBody updateBody
    ) {
        return projectService.updateProject(memberId, projectId, updateBody);
    }

    @DeleteMapping("/{memberId}/{projectId}")
    public void deleteProject(
        @PathVariable Integer memberId,
        @PathVariable Integer projectId
    ) {
        projectService.deleteProject(memberId, projectId);
    }

    @PostMapping("/{ownerId}/{projectId}/members/{newUserId}")
    public ProjectDTO addMember(
        @PathVariable Integer ownerId,
        @PathVariable Integer projectId,
        @PathVariable Integer newUserId
    ) {
        return projectService.addMember(ownerId, projectId, newUserId);
    }

    @DeleteMapping("/{ownerId}/{projectId}/members/{memberId}")
    public void removeMember(
        @PathVariable Integer ownerId,
        @PathVariable Integer projectId,
        @PathVariable Integer memberId
    ) {
        projectService.removeMember(ownerId, projectId, memberId);
    }

    @GetMapping("/{memberId}/{projectId}/members")
    public List<MemberDTO> getProjectMembers(
            @PathVariable Integer memberId,
            @PathVariable Integer projectId
    ) {
        return projectService.getProjectMembers(memberId, projectId);
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
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(e.getMessage());
    }
} 
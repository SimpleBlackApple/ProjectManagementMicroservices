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

    @GetMapping("/member/{memberId}")
    public List<ProjectDTO> getProject(@PathVariable Integer memberId) {
        // 获取该成员作为owner的所有项目
        return projectService.getProjectByOwnerId(memberId);
    }

    @GetMapping("/all/{memberId}")
    public List<ProjectDTO> getAllProjects(@PathVariable Integer memberId) {
        // 获取该成员参与的所有项目（包括作为owner和普通成员的）
        return projectService.getAllProjects(memberId);
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

    @GetMapping("/{projectId}/members")
    public List<MemberDTO> getProjectMembers(@PathVariable Integer projectId) {
        return projectService.getProjectMembers(projectId);
    }

} 
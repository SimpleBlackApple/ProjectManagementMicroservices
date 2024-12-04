package org.apache.dubbo.samples.seata.project.service;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.samples.seata.api.ProjectService;
import org.apache.dubbo.samples.seata.api.UserService;
import org.apache.dubbo.samples.seata.api.dto.*;
import org.apache.dubbo.samples.seata.project.entity.Project;
import org.apache.dubbo.samples.seata.project.repository.ProjectRepository;
import org.apache.dubbo.samples.seata.api.util.BeanCopyUtils;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@DubboService
public class ProjectServiceImpl implements ProjectService {

    @Resource
    private ProjectRepository projectRepository;

    @DubboReference(check = false)
    private UserService userService;

    @Override
    @GlobalTransactional
    public ProjectDTO createProject(ProjectCreateBody createBody) {
        // 验证用户是否存在
        UserDTO user = userService.getUserById(createBody.getOwnerId());
        
        Project project = new Project();
        BeanUtils.copyProperties(createBody, project);
        project.setOwnerId(user.getUserId());
        project.setCreatedAt(LocalDateTime.now());
        project.setStatus("IN_PROGRESS");
        
        return convertToProjectDTO(projectRepository.save(project));
    }

    @Override
    @GlobalTransactional
    public ProjectDTO updateProject(Integer projectId, ProjectUpdateBody updateBody) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
            
        // 验证用户是否存在
        UserDTO user = userService.getUserById(updateBody.getOwnerId());
        
        // 验证更新权限
        if (!project.getOwnerId().equals(user.getUserId())) {
            throw new RuntimeException("Only project owner can update the project");
        }

        BeanCopyUtils.copyNonNullProperties(updateBody, project);
        return convertToProjectDTO(projectRepository.save(project));
    }

    @Override
    @GlobalTransactional
    public void deleteProject(Integer projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
            
        // 验证用户是否存在
        userService.getUserById(project.getOwnerId());
        
        projectRepository.deleteById(projectId);
    }

    @Override
    public ProjectDTO getProjectById(Integer projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        return convertToProjectDTO(project);
    }

    @Override
    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToProjectDTO)
                .collect(Collectors.toList());
    }

    private ProjectDTO convertToProjectDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        BeanUtils.copyProperties(project, dto);
        return dto;
    }
}
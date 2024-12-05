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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@DubboService
@Service
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
    public ProjectDTO updateProject(Integer userId, Integer projectId, ProjectUpdateBody updateBody) {
        // 1. 验证项目是否存在
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // 2. 验证当前用户是否存在
        UserDTO currentUser = userService.getUserById(userId);
        if (currentUser == null) {
            throw new RuntimeException("Current user not found");
        }
        
        // 3. 如果更新请求包含新的 ownerId，验证新 owner 是否存在
        if (updateBody.getOwnerId() != null) {
            UserDTO newOwner = userService.getUserById(updateBody.getOwnerId());
            if (newOwner == null) {
                throw new RuntimeException("New owner not found");
            }
        }
        
        // 4. 验证当前用户是否有权限更新项目（必须是项目的当前所有者）
        if (!project.getOwnerId().equals(userId)) {
            throw new RuntimeException("Only project owner can update the project");
        }

        // 5. 更新项目
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
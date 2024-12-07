package org.apache.dubbo.samples.seata.project.service;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.samples.seata.api.ProjectService;
import org.apache.dubbo.samples.seata.api.UserService;
import org.apache.dubbo.samples.seata.api.dto.*;
import org.apache.dubbo.samples.seata.project.entity.Project;
import org.apache.dubbo.samples.seata.project.entity.ProjectMember;
import org.apache.dubbo.samples.seata.project.repository.ProjectRepository;
import org.apache.dubbo.samples.seata.project.repository.ProjectMemberRepository;
import org.apache.dubbo.samples.seata.api.util.BeanCopyUtils;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@DubboService
@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    @Resource
    private ProjectRepository projectRepository;

    @Resource
    private ProjectMemberRepository projectMemberRepository;

    @DubboReference(check = false)
    private UserService userService;

    private void validateMembership(Integer projectId, Integer userId) {
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new RuntimeException("Access denied: user is not a member of this project");
        }
    }

    @Override
    @GlobalTransactional
    public ProjectDTO createProject(Integer ownerId, ProjectCreateBody createBody) {
        // 验证创建者是否存在
        userService.getUserById(ownerId);
        
        // 创建项目
        Project project = new Project();
        BeanUtils.copyProperties(createBody, project);
        project.setOwnerId(ownerId);
        project.setCreatedAt(LocalDateTime.now());
        project.setStatus("IN_PROGRESS");
        
        // 添加owner作为成员
        project.addMember(ownerId);
        
        project = projectRepository.save(project);
        return convertToProjectDTO(project);
    }

    @Override
    @GlobalTransactional
    public ProjectDTO updateProject(Integer memberId, Integer projectId, ProjectUpdateBody updateBody) {
        // 验证项目是否存在
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // 验证用户是否是项目成员
        validateMembership(projectId, memberId);

        BeanCopyUtils.copyNonNullProperties(updateBody, project);
        return convertToProjectDTO(projectRepository.save(project));
    }

    @Override
    @GlobalTransactional
    public void deleteProject(Integer memberId, Integer projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // 验证用户是否是项目所有者
        if (!project.getOwnerId().equals(memberId)) {
            throw new RuntimeException("Only project owner can delete the project");
        }
        
        projectRepository.deleteById(projectId);
    }

    @Override
    public List<ProjectDTO> getProjectByOwnerId(Integer ownerId) {
        // 获取该用户作为owner的所有项目
        return projectRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::convertToProjectDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectDTO> getAllProjects() {
        // 直接返回所有项目
        return projectRepository.findAll()
                .stream()
                .map(this::convertToProjectDTO)
                .collect(Collectors.toList());
    }

    @Override
    @GlobalTransactional
    public ProjectDTO addMember(Integer ownerId, Integer projectId, Integer newUserId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        if (!project.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Only project owner can add members");
        }
        
        // 验证新成员是否存在
        userService.getUserById(newUserId);
        
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, newUserId)) {
            throw new RuntimeException("User is already a member of this project");
        }
        
        project.addMember(newUserId);
        project = projectRepository.save(project);
        
        return convertToProjectDTO(project);
    }

    @Override
    @GlobalTransactional
    public void removeMember(Integer ownerId, Integer projectId, Integer memberId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // 验证是否是项目所有者
        if (!project.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Only project owner can remove members");
        }
        
        project.removeMember(memberId);
        projectRepository.save(project);
    }

    @Override
    public List<MemberDTO> getProjectMembers(Integer memberId, Integer projectId) {
        // 验证请求用户是否是项目成员
        validateMembership(projectId, memberId);
        
        List<ProjectMember> projectMembers = projectMemberRepository.findByProjectId(projectId);
        return projectMembers.stream()
            .map(pm -> {
                MemberDTO dto = new MemberDTO();
                dto.setUserId(pm.getUserId());
                dto.setJoinedAt(pm.getJoinedAt());
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public ProjectDTO getProject(Integer memberId, Integer projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // 验证用户是否是项目成员
        validateMembership(projectId, memberId);
        
        return convertToProjectDTO(project);
    }

    private ProjectDTO convertToProjectDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        BeanUtils.copyProperties(project, dto);
        
        // 添加成员信息
        dto.setMembers(project.getProjectMembers().stream()
            .map(pm -> {
                MemberDTO memberDTO = new MemberDTO();
                memberDTO.setUserId(pm.getUserId());
                memberDTO.setJoinedAt(pm.getJoinedAt());
                return memberDTO;
            })
            .collect(Collectors.toList()));
            
        return dto;
    }
}
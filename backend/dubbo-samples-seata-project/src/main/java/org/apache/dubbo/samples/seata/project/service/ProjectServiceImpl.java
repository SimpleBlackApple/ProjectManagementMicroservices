package org.apache.dubbo.samples.seata.project.service;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.samples.seata.api.ProjectService;
import org.apache.dubbo.samples.seata.api.UserService;
import org.apache.dubbo.samples.seata.api.TaskService;
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
import java.util.ArrayList;

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

    @DubboReference(check = false)
    private TaskService taskService;

    private void validateMembership(Integer projectId, Integer userId) {
        if (!projectMemberRepository.existsByProjectIdAndUserIdAndDeletedFalse(projectId, userId)) {
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
        try {
            Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
            
            // 验证用户是否是项目所有者
            if (!project.getOwnerId().equals(memberId)) {
                throw new RuntimeException("Only project owner can delete the project");
            }
            
            // 先删除项目相关的 sprints 和 tasks
            taskService.deleteProjectRelatedItems(projectId);
            
            // 删除项目成员关系
            projectMemberRepository.deleteByProjectId(projectId);
            
            // 最后删除项目
            projectRepository.deleteById(projectId);
        } catch (Exception e) {
            // 添加更详细的错误信息
            String errorMessage = String.format(
                "Failed to delete project %d: %s", 
                projectId, 
                e.getMessage()
            );
            throw new RuntimeException(errorMessage);
        }
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
    public ProjectDTO addMember(Integer userId, Integer projectId, Integer newMemberId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
            
        // 验证权限
        if (!userId.equals(project.getOwnerId())) {
            throw new RuntimeException("Only project owner can add members");
        }

        // 添加新成员
        ProjectMember newMember = new ProjectMember();
        newMember.setProject(project);
        newMember.setUserId(newMemberId);
        newMember.setJoinedAt(LocalDateTime.now());
        projectMemberRepository.save(newMember);

        // 转换为DTO并手动添加新成员信息
        ProjectDTO projectDTO = convertToProjectDTO(project);
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setUserId(newMemberId);
        memberDTO.setJoinedAt(newMember.getJoinedAt());
        projectDTO.getMembers().add(memberDTO);

        return projectDTO;
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
        // 验证请求户是否是项目成员
        validateMembership(projectId, memberId);
        
        List<ProjectMember> projectMembers = projectMemberRepository.findByProjectId(projectId);
        return projectMembers.stream()
            .map(pm -> {
                MemberDTO dto = new MemberDTO();
                dto.setUserId(pm.getUserId());
                dto.setJoinedAt(pm.getJoinedAt());
                dto.setDeleted(pm.isDeleted());
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

    @Override
    @GlobalTransactional
    public void handleUserDeletion(Integer userId) {
        // 获取用户拥有的所有项目
        List<Project> ownedProjects = projectRepository.findByOwnerId(userId);

        for (Project project : ownedProjects) {
            List<ProjectMember> members = projectMemberRepository.findByProjectIdAndDeletedFalseOrderByJoinedAtAsc(project.getId());

            // 如果项目只有owner一个成员，删除整个项目及其相关内容
            if (members.size() <= 1) {
                try {
                    // 先删除项目相关的 sprints 和 tasks
                    taskService.deleteProjectRelatedItems(project.getId());
                    
                    // 删除项目成员关系
                    projectMemberRepository.deleteByProjectId(project.getId());
                    
                    // 最后删除项目
                    projectRepository.delete(project);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to delete project: " + e.getMessage());
                }
                continue;
            }

            // 找到最早加入的非owner成员
            ProjectMember earliestMember = members.stream()
                .filter(m -> !m.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No valid member found to transfer ownership"));

            // 转移所有权
            project.setOwnerId(earliestMember.getUserId());
            projectRepository.save(project);
        }

        // 将用户作为成员的所有项目记录标记为已删除
        List<ProjectMember> memberProjects = projectMemberRepository.findByUserIdAndDeletedFalseOrderByJoinedAtAsc(userId);
        memberProjects.forEach(member -> {
            member.setDeleted(true);
            projectMemberRepository.save(member);
        });
    }

    @Override
    public boolean isUserProjectOwner(Integer userId) {
        return !projectRepository.findByOwnerId(userId).isEmpty();
    }

    @Override
    @GlobalTransactional
    public void deleteProjectRollback(Integer memberId, Integer projectId) {
        try {
            Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
            
            // 验证用户是否是项目所有者
            if (!project.getOwnerId().equals(memberId)) {
                throw new RuntimeException("Only project owner can delete the project");
            }
            
            // 先删除项目相关的 sprints 和 tasks
            taskService.deleteProjectRelatedItems(projectId);
            
            // 删除项目成员关系
            projectMemberRepository.deleteByProjectId(projectId);
            
            // 最后删除项目
            projectRepository.deleteById(projectId);
            
            // 抛出异常触发回滚
            throw new RuntimeException("Simulated error for testing rollback");
        } catch (Exception e) {
            String errorMessage = String.format(
                "Failed to delete project %d: %s", 
                projectId, 
                e.getMessage()
            );
            throw new RuntimeException(errorMessage);
        }
    }

    private ProjectDTO convertToProjectDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        BeanUtils.copyProperties(project, dto);
        
        // 初始化空列表，避免NPE
        dto.setMembers(new ArrayList<>());
        
        return dto;
    }
}
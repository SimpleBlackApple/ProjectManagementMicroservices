package org.apache.dubbo.samples.seata.project.service;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.samples.seata.api.ProjectService;
import org.apache.dubbo.samples.seata.api.TaskService;
import org.apache.dubbo.samples.seata.api.dto.*;
import org.apache.dubbo.samples.seata.project.entity.Project;
import org.apache.dubbo.samples.seata.project.entity.ProjectMember;
import org.apache.dubbo.samples.seata.project.entity.User;
import org.apache.dubbo.samples.seata.project.repository.ProjectRepository;
import org.apache.dubbo.samples.seata.project.repository.ProjectMemberRepository;
import org.apache.dubbo.samples.seata.api.util.BeanCopyUtils;
import org.apache.dubbo.samples.seata.project.repository.UserRepository;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UserDetails;
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
    private TaskService taskService;

    @Resource
    private UserRepository userRepository;

    private void validateMembership(Integer projectId, Integer userId) {
        if (!projectMemberRepository.existsByProjectIdAndUserIdAndDeletedFalse(projectId, userId)) {
            throw new RuntimeException("Access denied: user is not a member of this project");
        }
    }

    @Override
    public ProjectDTO createProject(UserDetails owner, ProjectCreateBody createBody) {
        // 创建项目
        Project project = new Project();
        BeanUtils.copyProperties(createBody, project);
        project.setOwner(owner);
        project.setCreatedAt(LocalDateTime.now());
        project.setStatus("IN_PROGRESS");
        
        // 添加owner作为员
        project.addMember(owner);
        
        project = projectRepository.save(project);
        return convertToProjectDTO(project);
    }

    @Override
    public ProjectDTO updateProject(UserDetails user, Integer projectId, ProjectUpdateBody updateBody) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // 验证用户是否是项目成员
        validateMembership(projectId, ((User) user).getId());

        BeanCopyUtils.copyNonNullProperties(updateBody, project);
        return convertToProjectDTO(projectRepository.save(project));
    }

    @Override
    @GlobalTransactional
    public void deleteProject(UserDetails user, Integer projectId) {
        try {
            Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
            
            // 验证用户是否是项目所有者
            if (!project.getOwner().getId().equals(((User) user).getId())) {
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
    public List<ProjectDTO> getProjectByOwner(UserDetails owner) {
        User user = (User) owner;
        return projectRepository.findByOwnerId(user.getId())
                .stream()
                .map(this::convertToProjectDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectDTO> getAllProjects(UserDetails member) {
        User user = (User) member;
        Integer userId = user.getId();
        
        // 获取用户参与的所有项目（包括作为owner和普通成员的）
        List<ProjectMember> memberProjects = projectMemberRepository
            .findByUserIdAndDeletedFalseOrderByJoinedAtAsc(userId);
            
        return memberProjects.stream()
            .map(ProjectMember::getProject)
            .distinct()  // 去重以防一个用户在同一个项目中有多个角色
            .map(this::convertToProjectDTO)
            .collect(Collectors.toList());
    }

    @Override
    public ProjectDTO addMember(UserDetails owner, Integer projectId, Integer newUserId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // 验证权限
        if (!project.getOwner().getId().equals(((User)owner).getId())) {
            throw new RuntimeException("Only project owner can add members");
        }

        // 检查用户是否已经是项目成员
        if (projectMemberRepository.existsByProjectIdAndUserIdAndDeletedFalse(projectId, newUserId)) {
            throw new RuntimeException("User is already a member of this project");
        }

        // 添加新成员
        ProjectMember newMember = new ProjectMember();
        newMember.setProject(project);
        newMember.setUser(userRepository.getReferenceById(newUserId));
        newMember.setJoinedAt(LocalDateTime.now());
        
        try {
            projectMemberRepository.save(newMember);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add member. User might already be a member of this project.");
        }

        // 转换为DTO并手动添加新成员信息
        ProjectDTO projectDTO = convertToProjectDTO(project);
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setUserId(newUserId);
        memberDTO.setJoinedAt(newMember.getJoinedAt());
        projectDTO.getMembers().add(memberDTO);

        return projectDTO;
    }

    @Override
    public void removeMember(UserDetails owner, Integer projectId, Integer memberId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // 验证是否是项目所有者
        if (!project.getOwner().getId().equals(((User) owner).getId())) {
            throw new RuntimeException("Only project owner can remove members");
        }
        
        // 禁止删除项目所有者的成员关系
        if (project.getOwner().getId().equals(memberId)) {
            throw new RuntimeException("Cannot remove project owner from members");
        }
        
        project.removeMember(userRepository.getReferenceById(memberId));
        projectRepository.save(project);
    }

    @Override
    public List<MemberDTO> getProjectMembers(UserDetails user, Integer projectId) {
        // 验证请求用户是否是项目成员
        validateMembership(projectId, ((User) user).getId());
        
        List<ProjectMember> projectMembers = projectMemberRepository.findByProjectId(projectId);
        return projectMembers.stream()
            .map(pm -> {
                MemberDTO dto = new MemberDTO();
                dto.setUserId(pm.getUser().getId());
                dto.setJoinedAt(pm.getJoinedAt());
                dto.setDeleted(pm.isDeleted());
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public ProjectDTO getProject(UserDetails user, Integer projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // 验证用户是否是项目成员
        validateMembership(projectId, ((User) user).getId());
        
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
                .filter(m -> !m.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No valid member found to transfer ownership"));

            // 转移所有权
            project.setOwner(earliestMember.getUser());
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
    public void deleteProjectRollback(UserDetails user, Integer projectId) {
        try {
            Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
            
            // 验证用户否是项目所有者
            if (!project.getOwner().getId().equals(((User) user).getId())) {
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

    @Override
    @GlobalTransactional
    public void syncNewUser(Integer userId, String email) {
        // 检查用户是否已存在
        if (userRepository.existsById(userId)) {
            throw new RuntimeException("User already exists in project service");
        }

        // 创建用户最小副本
        User user = User.builder()
                .id(userId)
                .email(email)
                .build();

        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sync user: " + e.getMessage());
        }
    }

    @Override
    @GlobalTransactional
    public void removeUserData(Integer userId) {
        userRepository.findById(userId).ifPresent(user -> {
            // 删除用户相关的项目成员记录
            List<ProjectMember> memberProjects = projectMemberRepository
                .findByUserIdAndDeletedFalseOrderByJoinedAtAsc(userId);
            projectMemberRepository.deleteAll(memberProjects);
            
            // 删除用户数据
            userRepository.delete(user);
        });
    }

    @Override
    @Transactional
    public ProjectDTO transferOwnership(UserDetails currentOwner, Integer projectId, Integer newOwnerId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // 验证当前用户是否是项目所有者
        if (!project.getOwner().getId().equals(((User) currentOwner).getId())) {
            throw new RuntimeException("Only project owner can transfer ownership");
        }
        
        // 验证新所有者是否是项目成员
        if (!projectMemberRepository.existsByProjectIdAndUserIdAndDeletedFalse(projectId, newOwnerId)) {
            throw new RuntimeException("New owner must be an existing project member");
        }
        
        // 转移所有权
        User newOwner = userRepository.findById(newOwnerId)
            .orElseThrow(() -> new RuntimeException("New owner not found"));
        project.setOwner(newOwner);
        
        project = projectRepository.save(project);
        return convertToProjectDTO(project);
    }

    private ProjectDTO convertToProjectDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        BeanUtils.copyProperties(project, dto);
        dto.setOwnerId(project.getOwner().getId());
        
        // 初始化空列表，避免NPE
        dto.setMembers(new ArrayList<>());
        
        return dto;
    }
}
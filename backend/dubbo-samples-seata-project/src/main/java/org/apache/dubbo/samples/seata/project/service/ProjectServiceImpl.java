package org.apache.dubbo.samples.seata.project.service;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.samples.seata.api.service.ProjectService;
import org.apache.dubbo.samples.seata.api.service.TaskService;
import org.apache.dubbo.samples.seata.api.dto.*;
import org.apache.dubbo.samples.seata.project.entity.Project;
import org.apache.dubbo.samples.seata.project.entity.ProjectMember;
import org.apache.dubbo.samples.seata.api.entity.User;
import org.apache.dubbo.samples.seata.project.repository.ProjectRepository;
import org.apache.dubbo.samples.seata.project.repository.ProjectMemberRepository;
import org.apache.dubbo.samples.seata.api.util.BeanCopyUtils;
import org.apache.dubbo.samples.seata.project.repository.UserRepository;
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
    private TaskService taskService;

    @Resource
    private UserRepository userRepository;

    private void validateMembership(Integer projectId, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (!projectMemberRepository.existsByProjectIdAndUserIdAndDeletedFalse(projectId, user.getId())) {
            throw new RuntimeException("Access denied: user is not a member of this project");
        }
    }

    @Override
    public ProjectDTO createProject(String email, ProjectCreateBody createBody) {
        User owner = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Project project = new Project();
        BeanUtils.copyProperties(createBody, project);
        project.setOwner(owner);
        project.setCreatedAt(LocalDateTime.now());
        project.setStatus("IN_PROGRESS");
        
        project.addMember(owner);
        
        project = projectRepository.save(project);
        return convertToProjectDTO(project);
    }

    @Override
    public ProjectDTO updateProject(String email, Integer projectId, ProjectUpdateBody updateBody) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        validateMembership(projectId, email);

        BeanCopyUtils.copyNonNullProperties(updateBody, project);
        return convertToProjectDTO(projectRepository.save(project));
    }

    @Override
    @GlobalTransactional
    public void deleteProject(String email, Integer projectId) {
        try {
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
            
            if (!project.getOwner().getId().equals(user.getId())) {
                throw new RuntimeException("Only project owner can delete the project");
            }
            
            taskService.deleteProjectRelatedItems(projectId);
            projectMemberRepository.deleteByProjectId(projectId);
            projectRepository.deleteById(projectId);
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
    public List<ProjectDTO> getProjectByOwner(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return projectRepository.findByOwnerId(user.getId())
                .stream()
                .map(this::convertToProjectDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectDTO> getAllProjects(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Integer userId = user.getId();
        
        List<ProjectMember> memberProjects = projectMemberRepository
            .findByUserIdAndDeletedFalseOrderByJoinedAtAsc(userId);
            
        return memberProjects.stream()
            .map(ProjectMember::getProject)
            .distinct()
            .map(this::convertToProjectDTO)
            .collect(Collectors.toList());
    }

    @Override
    public ProjectDTO addMember(String ownerEmail, Integer projectId, String newUserEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
            .orElseThrow(() -> new RuntimeException("Owner not found"));
        User newUser = userRepository.findByEmail(newUserEmail)
            .orElseThrow(() -> new RuntimeException("New user not found"));
        
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        if (!project.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Only project owner can add members");
        }

        if (projectMemberRepository.existsByProjectIdAndUserIdAndDeletedFalse(projectId, newUser.getId())) {
            throw new RuntimeException("User is already a member of this project");
        }

        ProjectMember newMember = new ProjectMember();
        newMember.setProject(project);
        newMember.setUser(newUser);
        newMember.setJoinedAt(LocalDateTime.now());
        
        projectMemberRepository.save(newMember);

        ProjectDTO projectDTO = convertToProjectDTO(project);
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setUserId(newUser.getId());
        memberDTO.setJoinedAt(newMember.getJoinedAt());
        projectDTO.getMembers().add(memberDTO);

        return projectDTO;
    }

    @Override
    public void removeMember(String ownerEmail, Integer projectId, String memberEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
            .orElseThrow(() -> new RuntimeException("Owner not found"));
        User member = userRepository.findByEmail(memberEmail)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        if (!project.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Only project owner can remove members");
        }
        
        if (project.getOwner().getId().equals(member.getId())) {
            throw new RuntimeException("Cannot remove project owner from members");
        }
        
        project.removeMember(member);
        projectRepository.save(project);
    }

    @Override
    public List<MemberDTO> getProjectMembers(String email, Integer projectId) {
        validateMembership(projectId, email);
        
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
    @GlobalTransactional
    public ProjectDTO getProject(String email, Integer projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        validateMembership(projectId, email);
        
        return convertToProjectDTO(project);
    }

    @Override
    @GlobalTransactional
    public void handleUserDeletion(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Integer userId = user.getId();
        
        List<Project> ownedProjects = projectRepository.findByOwnerId(userId);

        for (Project project : ownedProjects) {
            List<ProjectMember> members = projectMemberRepository.findByProjectIdAndDeletedFalseOrderByJoinedAtAsc(project.getId());

            if (members.size() <= 1) {
                try {
                    taskService.deleteProjectRelatedItems(project.getId());
                    projectMemberRepository.deleteByProjectId(project.getId());
                    projectRepository.delete(project);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to delete project: " + e.getMessage());
                }
                continue;
            }

            ProjectMember earliestMember = members.stream()
                .filter(m -> !m.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No valid member found to transfer ownership"));

            project.setOwner(earliestMember.getUser());
            projectRepository.save(project);
        }

        List<ProjectMember> memberProjects = projectMemberRepository.findByUserIdAndDeletedFalseOrderByJoinedAtAsc(userId);
        memberProjects.forEach(member -> {
            member.setDeleted(true);
            projectMemberRepository.save(member);
        });
    }

    @Override
    public boolean isUserProjectOwner(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return !projectRepository.findByOwnerId(user.getId()).isEmpty();
    }

    @Override
    @GlobalTransactional
    public void deleteProjectRollback(String email, Integer projectId) {
        try {
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
            
            if (!project.getOwner().getId().equals(user.getId())) {
                throw new RuntimeException("Only project owner can delete the project");
            }
            
            taskService.deleteProjectRelatedItems(projectId);
            projectMemberRepository.deleteByProjectId(projectId);
            projectRepository.deleteById(projectId);
            
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
    public void syncNewUser(Integer userId, String name, String email, String password) {
        // 检查用户是否已存在
        if (userRepository.existsById(userId)) {
            throw new RuntimeException("User already exists in project service");
        }

        // 创建完整用户对象
        User user = User.builder()
                .id(userId)
                .name(name)
                .email(email)
                .password(password)
                .createdAt(LocalDateTime.now())
                .build();

        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sync user: " + e.getMessage());
        }
    }

    @Override
    @GlobalTransactional
    public void removeUserData(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Integer userId = user.getId();
        userRepository.findById(userId).ifPresent(u -> {
            List<ProjectMember> memberProjects = projectMemberRepository
                .findByUserIdAndDeletedFalseOrderByJoinedAtAsc(userId);
            projectMemberRepository.deleteAll(memberProjects);
            userRepository.delete(u);
        });
    }

    @Override
    @Transactional
    public ProjectDTO transferOwnership(String currentOwnerEmail, Integer projectId, String newOwnerEmail) {
        User currentOwner = userRepository.findByEmail(currentOwnerEmail)
            .orElseThrow(() -> new RuntimeException("Current owner not found"));
        User newOwner = userRepository.findByEmail(newOwnerEmail)
            .orElseThrow(() -> new RuntimeException("New owner not found"));
        
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        if (!project.getOwner().getId().equals(currentOwner.getId())) {
            throw new RuntimeException("Only project owner can transfer ownership");
        }
        
        if (!projectMemberRepository.existsByProjectIdAndUserIdAndDeletedFalse(projectId, newOwner.getId())) {
            throw new RuntimeException("New owner must be an existing project member");
        }
        
        project.setOwner(newOwner);
        
        project = projectRepository.save(project);
        return convertToProjectDTO(project);
    }

    @Override
    @GlobalTransactional
    public boolean validateUserProject(String email, Integer projectId) {
        try {
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            if (!projectRepository.existsById(projectId)) {
                return false;
            }
            return projectMemberRepository.existsByProjectIdAndUserIdAndDeletedFalse(projectId, user.getId());
        } catch (Exception e) {
            return false;
        }
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
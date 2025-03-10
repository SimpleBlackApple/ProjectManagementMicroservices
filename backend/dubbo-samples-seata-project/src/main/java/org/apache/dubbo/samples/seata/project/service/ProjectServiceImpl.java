package org.apache.dubbo.samples.seata.project.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.samples.seata.api.dto.MemberDTO;
import org.apache.dubbo.samples.seata.api.dto.ProjectCreateBody;
import org.apache.dubbo.samples.seata.api.dto.ProjectDTO;
import org.apache.dubbo.samples.seata.api.dto.ProjectUpdateBody;
import org.apache.dubbo.samples.seata.api.dto.UserDTO;
import org.apache.dubbo.samples.seata.api.entity.User;
import org.apache.dubbo.samples.seata.api.service.ProjectService;
import org.apache.dubbo.samples.seata.api.service.TaskService;
import org.apache.dubbo.samples.seata.api.service.UserService;
import org.apache.dubbo.samples.seata.api.util.BeanCopyUtils;
import org.apache.dubbo.samples.seata.project.entity.Project;
import org.apache.dubbo.samples.seata.project.entity.ProjectMember;
import org.apache.dubbo.samples.seata.project.repository.ProjectMemberRepository;
import org.apache.dubbo.samples.seata.project.repository.ProjectRepository;
import org.apache.dubbo.samples.seata.project.repository.UserRepository;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@DubboService
@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);
    @Resource
    private ProjectRepository projectRepository;

    @Resource
    private ProjectMemberRepository projectMemberRepository;

    @DubboReference(check = false)
    private TaskService taskService;

    @Resource
    private UserRepository userRepository;

    @DubboReference(check = false)
    private UserService userService;

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
        return convertToProjectDTO(project, owner.getOriginId());
    }

    @Override
    public ProjectDTO updateProject(String email, Integer projectId, ProjectUpdateBody updateBody) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        validateMembership(projectId, email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BeanCopyUtils.copyNonNullProperties(updateBody, project);
        return convertToProjectDTO(projectRepository.save(project), user.getOriginId());
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
                .map(project -> convertToProjectDTO(project, user.getOriginId()))
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
                .map(project -> {
                    User owner = project.getOwner();
                    return convertToProjectDTO(project, owner.getOriginId());
                })
                .collect(Collectors.toList());
    }

    @Override
    @GlobalTransactional
    public ProjectDTO addMember(String ownerEmail, Integer projectId, Integer newUserId) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        UserDTO newUserDTO = userService.getUserById(newUserId);
        if (newUserDTO == null) {
            throw new RuntimeException("New user not found");
        }

        // 首先尝试通过 ID 查找用户
        User newUser = userRepository.findById(newUserId)
                .orElseGet(() -> {
                    // 如果通过 ID 找不到，再尝试通过 email 查找
                    return userRepository.findByEmail(newUserDTO.getEmail())
                            .orElseGet(() -> {
                                // 如果都找不到，创建新用户
                                User user = new User();
                                user.setId(newUserId);  // 设置原始 ID
                                user.setEmail(newUserDTO.getEmail());
                                user.setName(newUserDTO.getName());
                                return userRepository.save(user);
                            });
                });

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

        ProjectDTO projectDTO = convertToProjectDTO(project, project.getOwner().getOriginId());
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setUserId(newUserId);  // 使用原始 newUserId
        memberDTO.setJoinedAt(newMember.getJoinedAt());
        projectDTO.getMembers().add(memberDTO);

        return projectDTO;
    }

    @Override
    @GlobalTransactional
    public void removeMember(String ownerEmail, Integer projectId, Integer memberId) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        // 直接使用 memberId 查找用户
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found in project service"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Only project owner can remove members");
        }

        if (project.getOwner().getId().equals(memberId)) {
            throw new RuntimeException("Cannot remove project owner from members");
        }

        ProjectMember projectMember = projectMemberRepository
                .findByProjectIdAndUserId(projectId, memberId)
                .orElseThrow(() -> new RuntimeException("Member not found in project"));

        projectMember.setDeleted(true);
        projectMemberRepository.save(projectMember);
    }

    @Override
    public List<MemberDTO> getProjectMembers(String email, Integer projectId) {
        validateMembership(projectId, email);

        List<ProjectMember> projectMembers = projectMemberRepository.findByProjectId(projectId);
        return projectMembers.stream()
                .map(pm -> {
                    MemberDTO dto = new MemberDTO();
                    // 直接从 ProjectMember 中获取用户 ID
                    dto.setUserId(pm.getUser().getOriginId());
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

        return convertToProjectDTO(project, project.getOwner().getOriginId());
    }

    @Override
    @GlobalTransactional
    public void handleUserDeletion(String email) {
        log.info("Handling user deletion");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Integer userId = user.getId();

        List<Project> ownedProjects = projectRepository.findByOwnerId(userId);

        for (Project project : ownedProjects) {
            log.info("Deleting project {}", project.getId());
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
        log.info("User handling successful");
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
                .originId(userId)
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
        log.info("Removing user data");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Integer userId = user.getId();
        userRepository.findById(userId).ifPresent(u -> {
            List<ProjectMember> memberProjects = projectMemberRepository
                    .findByUserIdAndDeletedFalseOrderByJoinedAtAsc(userId);
            projectMemberRepository.deleteAll(memberProjects);
            userRepository.delete(u);
        });
        log.info("User deletion successful");
    }

    @Override
    @GlobalTransactional
    public ProjectDTO transferOwnership(String currentOwnerEmail, Integer projectId, Integer newOwnerId) {
        User currentOwner = userRepository.findByEmail(currentOwnerEmail)
                .orElseThrow(() -> new RuntimeException("Current owner not found"));

        UserDTO newOwnerDTO = userService.getUserById(newOwnerId);
        if (newOwnerDTO == null) {
            throw new RuntimeException("New owner not found");
        }

        User newOwner = userRepository.findByEmail(newOwnerDTO.getEmail())
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(newOwnerDTO.getEmail());
                    user.setName(newOwnerDTO.getName());
                    return userRepository.save(user);
                });

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
        return convertToProjectDTO(project, newOwner.getOriginId());
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

    private ProjectDTO convertToProjectDTO(Project project, Integer originId) {
        ProjectDTO dto = new ProjectDTO();
        BeanUtils.copyProperties(project, dto);
        dto.setOwnerId(originId);

        // 初始化空列表，避免NPE
        dto.setMembers(new ArrayList<>());

        return dto;
    }
}

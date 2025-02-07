package org.apache.dubbo.samples.seata.task.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.samples.seata.api.dto.SprintCreateBody;
import org.apache.dubbo.samples.seata.api.dto.SprintDTO;
import org.apache.dubbo.samples.seata.api.dto.SprintUpdateBody;
import org.apache.dubbo.samples.seata.api.dto.TaskCreateBody;
import org.apache.dubbo.samples.seata.api.dto.TaskDTO;
import org.apache.dubbo.samples.seata.api.dto.TaskUpdateBody;
import org.apache.dubbo.samples.seata.api.entity.User;
import org.apache.dubbo.samples.seata.api.service.ProjectService;
import org.apache.dubbo.samples.seata.api.service.TaskService;
import org.apache.dubbo.samples.seata.api.service.UserService;
import org.apache.dubbo.samples.seata.api.util.BeanCopyUtils;
import org.apache.dubbo.samples.seata.task.entity.Sprint;
import org.apache.dubbo.samples.seata.task.entity.Task;
import org.apache.dubbo.samples.seata.task.repository.SprintRepository;
import org.apache.dubbo.samples.seata.task.repository.TaskRepository;
import org.apache.dubbo.samples.seata.task.repository.UserRepository;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@DubboService
@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    @Resource
    private SprintRepository sprintRepository;

    @Resource
    private TaskRepository taskRepository;

    @DubboReference(check = false, timeout = 30000)
    private ProjectService projectService;
    @Autowired
    private UserRepository userRepository;

    @Resource
    private EntityManager entityManager;

    @DubboReference(check = false)
    private UserService userService;

    // Sprint 相关操作
    @Override
    @GlobalTransactional
    public SprintDTO getSprintById(String email, Integer sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));

        if (!projectService.validateUserProject(email, sprint.getProjectId())) {
            throw new RuntimeException("Access denied: user is not a member of this project");
        }

        return convertToSprintDTO(sprint);
    }

    @Override
    @GlobalTransactional
    public List<SprintDTO> getProjectSprints(String email, Integer projectId) {
        if (!projectService.validateUserProject(email, projectId)) {
            throw new RuntimeException("Access denied: user is not a member of this project");
        }

        return sprintRepository.findByProjectId(projectId).stream()
                .map(this::convertToSprintDTO)
                .collect(Collectors.toList());
    }

    @Override
    @GlobalTransactional(timeoutMills = 30000)
    public SprintDTO createSprint(String email, Integer projectId, SprintCreateBody createBody) {
        try {
            if (!projectService.validateUserProject(email, projectId)) {
                throw new RuntimeException("Project not found or user is not a member");
            }

            Sprint sprint = new Sprint();
            sprint.setName(createBody.getName());
            sprint.setProjectId(projectId);
            sprint.setStatus("TO_DO");
            sprint.setStartDate(createBody.getStartDate());
            sprint.setEndDate(createBody.getEndDate());

            validateSprintDates(sprint);

            return convertToSprintDTO(sprintRepository.save(sprint));
        } catch (Exception e) {
            if (e.getMessage().contains("Access denied") || e.getMessage().contains("not a member")) {
                throw new RuntimeException("Access denied: user is not a member of this project");
            }
            throw new RuntimeException("Failed to create sprint: " + e.getMessage());
        }
    }

    @Override
    @GlobalTransactional
    public SprintDTO updateSprint(String email, Integer sprintId, SprintUpdateBody updateBody) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));

        if (!projectService.validateUserProject(email, sprint.getProjectId())) {
            throw new RuntimeException("Access denied: user is not a member of this project");
        }

        if (updateBody.getStatus() != null) {
            validateSprintStatusChange(sprint, updateBody.getStatus());
        }

        BeanCopyUtils.copyNonNullProperties(updateBody, sprint);
        return convertToSprintDTO(sprintRepository.save(sprint));
    }

    @Override
    @GlobalTransactional
    public void deleteSprint(String email, Integer sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));

        if (!projectService.validateUserProject(email, sprint.getProjectId())) {
            throw new RuntimeException("Access denied: user is not a member of this project");
        }

        // 先解除所有任务的关联
        List<Task> tasks = taskRepository.findBySprintId(sprintId);
        for (Task task : tasks) {
            task.setSprint(null);
            taskRepository.save(task);
        }

        // 删除sprint
        sprintRepository.deleteById(sprintId);
    }

    // Task 相关操作
    @Override
    @GlobalTransactional
    public TaskDTO getTaskById(String email, Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!projectService.validateUserProject(email, task.getProjectId())) {
            throw new RuntimeException("Access denied: user is not a member of this project");
        }

        return convertToTaskDTO(task);
    }

    @Override
    @GlobalTransactional
    public List<TaskDTO> getProjectTasks(String email, Integer projectId) {
        if (!projectService.validateUserProject(email, projectId)) {
            throw new RuntimeException("Access denied: user is not a member of this project");
        }

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList());
    }

    @Override
    @GlobalTransactional
    public TaskDTO createTask(String email, Integer projectId, TaskCreateBody createBody) {
        try {
            if (!projectService.validateUserProject(email, projectId)) {
                throw new RuntimeException("Access denied: user is not a member of this project");
            }

            Task task = new Task();
            BeanUtils.copyProperties(createBody, task);
            task.setProjectId(projectId);
            task.setStatus(createBody.getStatus() != null ? createBody.getStatus() : "TO_DO");

            // 只有在指定了 managerId 时才设置负责人
            if (createBody.getManagerId() != null) {
                // 添加日志
                System.out.println("Input managerId: " + createBody.getManagerId());

                // 直接通过ID查找用户
                User member = userRepository.findByOriginId(createBody.getManagerId())
                        .orElseThrow(() -> new RuntimeException("Member not found with ID: " + createBody.getManagerId()));

                // 验证用户是否为项目成员
                if (!projectService.validateUserProject(member.getEmail(), projectId)) {
                    throw new RuntimeException("Assigned member is not a member of this project");
                }

                task.setMember(member);

                // 添加日志
                System.out.println("Set member ID: " + member.getId());
            }
            // 如果指定了 sprint，验证 sprint
            if (createBody.getSprintId() != null) {
                Sprint sprint = sprintRepository.findById(createBody.getSprintId())
                        .orElseThrow(() -> new RuntimeException("Sprint not found"));

                if (!sprint.getProjectId().equals(projectId)) {
                    throw new RuntimeException("Sprint does not belong to this project");
                }
                task.setSprint(sprint);
            }

            task.validateDates();

            return convertToTaskDTO(taskRepository.save(task));
        } catch (Exception e) {
            if (e.getMessage().contains("Access denied") || e.getMessage().contains("not a member")) {
                throw new RuntimeException("Access denied: user is not a member of this project");
            }
            throw new RuntimeException("Failed to create task: " + e.getMessage());
        }
    }

    @Override
    @GlobalTransactional
    public TaskDTO updateTask(String email, Integer taskId, TaskUpdateBody updateBody) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!projectService.validateUserProject(email, task.getProjectId())) {
            throw new RuntimeException("Access denied: user is not a member of this project");
        }

        // 更新负责人
        if (updateBody.getManagerId() != null) {
            User member = userRepository.findByOriginId(updateBody.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Member not found with ID: " + updateBody.getManagerId()));

            // 验证用户是否为项目成员
            if (!projectService.validateUserProject(member.getEmail(), task.getProjectId())) {
                throw new RuntimeException("Assigned member is not a member of this project");
            }

            task.setMember(member);
        }

        if (updateBody.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(updateBody.getSprintId())
                    .orElseThrow(() -> new RuntimeException("Sprint not found"));

            if (!sprint.getProjectId().equals(task.getProjectId())) {
                throw new RuntimeException("Sprint does not belong to this project");
            }
            task.setSprint(sprint);
        } else if (updateBody.getSprintId() == null) {
            task.setSprint(null);
        }

        BeanCopyUtils.copyNonNullProperties(updateBody, task);
        task.setUpdatedAt(LocalDateTime.now());
        task.validateDates();

        return convertToTaskDTO(taskRepository.save(task));
    }

    @Override
    @GlobalTransactional
    public void deleteTask(String email, Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!projectService.validateUserProject(email, task.getProjectId())) {
            throw new RuntimeException("Access denied: user is not a member of this project");
        }

        taskRepository.deleteById(taskId);
    }

    @Override
    @GlobalTransactional
    public List<TaskDTO> getSprintTasks(String email, Integer projectId, Integer sprintId) {
        if (!projectService.validateUserProject(email, projectId)) {
            throw new RuntimeException("Access denied: user is not a member of this project");
        }

        // 验证 sprint 是否属于该项目
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));

        if (!sprint.getProjectId().equals(projectId)) {
            throw new RuntimeException("Sprint does not belong to this project");
        }

        List<Task> tasks = taskRepository.findBySprintId(sprintId);
        return tasks.stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList());
    }

    @Override
    @GlobalTransactional
    public void deleteProjectRelatedItems(Integer projectId) {
        try {
            //System.out.println("pass1");
            // 删除所有相关的 tasks
            taskRepository.deleteByProjectId(projectId);
            //System.out.println("pass2");
            // 删除所有相关的 sprints
            sprintRepository.deleteByProjectId(projectId);
            //System.out.println("pass3");
        } catch (Exception e) {
            //System.out.println("error");
            throw new RuntimeException("Failed to delete project related items: " + e.getMessage());
        }
    }

    // 辅助方法
    private void validateSprintStatusChange(Sprint sprint, String newStatus) {
        if (newStatus.equals("IN_PROGRESS") && !sprint.getStatus().equals("TO_DO")) {
            throw new RuntimeException("Sprint can only be set to IN_PROGRESS from TO_DO status");
        }

        if (newStatus.equals("DONE")) {
            boolean hasUncompletedTasks = sprint.getTasks().stream()
                    .anyMatch(task -> !task.getStatus().equals("DONE"));
            if (hasUncompletedTasks) {
                throw new RuntimeException("Cannot complete sprint with uncompleted tasks");
            }
        }
    }

    private SprintDTO convertToSprintDTO(Sprint sprint) {
        SprintDTO dto = new SprintDTO();

        // 复制基本属性
        dto.setId(sprint.getId());
        dto.setProjectId(sprint.getProjectId());
        dto.setName(sprint.getName());
        dto.setStartDate(sprint.getStartDate());
        dto.setEndDate(sprint.getEndDate());
        dto.setStatus(sprint.getStatus());

        // 从数据库直接查询计算故事点
        List<Task> allTasks = taskRepository.findBySprintId(sprint.getId());
        int totalPoints = allTasks.stream()
                .mapToInt(Task::getStoryPoints)
                .sum();

        List<Task> completedTasks = taskRepository.findBySprintIdAndStatus(sprint.getId(), "DONE");
        int completedPoints = completedTasks.stream()
                .mapToInt(Task::getStoryPoints)
                .sum();

        dto.setTotalStoryPoints(totalPoints);
        dto.setCompletedStoryPoints(completedPoints);

        return dto;
    }

    private TaskDTO convertToTaskDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setType(task.getType());
        dto.setStatus(task.getStatus());
        dto.setProjectId(task.getProjectId());
        dto.setManagerId(task.getMember().getOriginId());
        dto.setStoryPoints(task.getStoryPoints());
        dto.setStartDate(task.getStartDate());
        dto.setDueDate(task.getDueDate());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        if (task.getSprint() != null) {
            dto.setSprintId(task.getSprint().getId());
        }

        return dto;
    }

    private void validateSprintDates(Sprint sprint) {
        if (sprint.getStartDate() == null || sprint.getEndDate() == null) {
            throw new RuntimeException("Start date and end date are required");
        }

        // if (sprint.getStartDate().isAfter(sprint.getEndDate())) {
        //     throw new RuntimeException("Start date must be before end date");
        // }
        // if (sprint.getStartDate().isBefore(LocalDateTime.now())) {
        //     throw new RuntimeException("Start date cannot be in the past");
        // }
    }

    @Override
    @GlobalTransactional
    public void syncNewUser(Integer userId, String name, String email, String password) {
        // 检查用户是否已存在
        if (userRepository.existsById(userId)) {
            throw new RuntimeException("User already exists in task service");
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
            throw new RuntimeException("Failed to sync user in task service: " + e.getMessage());
        }
    }

    @Override
    @GlobalTransactional
    public void removeUserData(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found in task service"));

        // 找到用户负责的所有任务
        List<Task> userTasks = taskRepository.findByMemberId(user.getId());

        // 将这些任务的负责人设置为null
        for (Task task : userTasks) {
            task.setMember(null);
            taskRepository.save(task);
        }

        // 删除用户数据
        userRepository.delete(user);
    }
}

package org.apache.dubbo.samples.seata.task.service;

import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.samples.seata.api.service.ProjectService;
import org.apache.dubbo.samples.seata.api.service.TaskService;
import org.apache.dubbo.samples.seata.api.dto.*;
import org.apache.dubbo.samples.seata.task.entity.Task;
import org.apache.dubbo.samples.seata.task.entity.Sprint;
import org.apache.dubbo.samples.seata.api.entity.User;
import org.apache.dubbo.samples.seata.task.repository.TaskRepository;
import org.apache.dubbo.samples.seata.task.repository.SprintRepository;
import org.apache.dubbo.samples.seata.api.util.BeanCopyUtils;
import org.apache.dubbo.samples.seata.task.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.seata.spring.annotation.GlobalTransactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

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
            User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!projectService.validateUserProject(email, projectId)) {
                throw new RuntimeException("Access denied: user is not a member of this project");
            }
            
            // 验证指定的负责人是否是项目成员
            Integer assigneeId = createBody.getManagerId();
            if (assigneeId != null) {
                User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new RuntimeException("Assigned user not found"));
                if (!projectService.validateUserProject(assignee.getEmail(), projectId)) {
                    throw new RuntimeException("Assigned member is not a member of this project");
                }
            } else {
                // 如果没有指定负责人，则设置为创建者
                assigneeId = creator.getId();
            }
            
            Task task = new Task();
            BeanUtils.copyProperties(createBody, task);
            task.setProjectId(projectId);
            task.setMember(userRepository.getReferenceById(assigneeId));
            task.setStatus("TO_DO");
            task.setCreatedAt(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            
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
        
        // 只有任务负责人可以更新任务
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (!task.getMember().getId().equals(user.getId())) {
            throw new RuntimeException("Only task assignee can update the task");
        }
        
        if (updateBody.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(updateBody.getSprintId())
                    .orElseThrow(() -> new RuntimeException("Sprint not found"));
                    
            if (!sprint.getProjectId().equals(task.getProjectId())) {
                throw new RuntimeException("Sprint does not belong to this project");
            }
            task.setSprint(sprint);
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

        // 只复制基本属性
        dto.setId(sprint.getId());
        dto.setProjectId(sprint.getProjectId());
        dto.setName(sprint.getName());
        dto.setStartDate(sprint.getStartDate());
        dto.setEndDate(sprint.getEndDate());
        dto.setStatus(sprint.getStatus());
        
        // 如果需要计算故事点，确保 tasks 集合已始化
        if (sprint.getTasks() != null) {
            dto.setTotalStoryPoints(sprint.getTotalStoryPoints());
            dto.setCompletedStoryPoints(sprint.getCompletedStoryPoints());
        } else {
            dto.setTotalStoryPoints(0);
            dto.setCompletedStoryPoints(0);
        }
        
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
        dto.setManagerId(task.getMember().getId());
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
        
        if (sprint.getStartDate().isAfter(sprint.getEndDate())) {
            throw new RuntimeException("Start date must be before end date");
        }
        
        if (sprint.getStartDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Start date cannot be in the past");
        }
    }

    @Override
    @GlobalTransactional
    public void syncNewUser(Integer userId, String name, String email, String password) {
        // 检查用户是否已���在
        if (userRepository.existsById(userId)) {
            throw new RuntimeException("User already exists in task service");
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
            throw new RuntimeException("Failed to sync user in task service: " + e.getMessage());
        }
    }
}
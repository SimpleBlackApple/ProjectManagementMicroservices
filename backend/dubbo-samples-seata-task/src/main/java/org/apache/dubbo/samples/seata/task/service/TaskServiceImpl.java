package org.apache.dubbo.samples.seata.task.service;

import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.samples.seata.api.ProjectService;
import org.apache.dubbo.samples.seata.api.TaskService;
import org.apache.dubbo.samples.seata.api.dto.*;
import org.apache.dubbo.samples.seata.task.entity.Task;
import org.apache.dubbo.samples.seata.task.entity.Sprint;
import org.apache.dubbo.samples.seata.task.repository.TaskRepository;
import org.apache.dubbo.samples.seata.task.repository.SprintRepository;
import org.apache.dubbo.samples.seata.api.util.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.seata.spring.annotation.GlobalTransactional;



import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@DubboService
@Service
public class TaskServiceImpl implements TaskService {

    @Resource
    private SprintRepository sprintRepository;

    @Resource
    private TaskRepository taskRepository;

    @DubboReference(check = false, timeout = 30000)
    private ProjectService projectService;

    // Sprint 相关操作
    @Override
    @GlobalTransactional
    public SprintDTO getSprintById(Integer memberId, Integer projectId, Integer sprintId) {
        // 验证用户和项目
        ProjectDTO project = projectService.getProject(memberId, projectId);
        if (project == null) {
            throw new RuntimeException("Project not found or user is not a member");
        }
        
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));
                
        if (!sprint.getProjectId().equals(projectId)) {
            throw new RuntimeException("Sprint does not belong to this project");
        }
        
        return convertToSprintDTO(sprint);
    }

    @Override
    @GlobalTransactional
    public List<SprintDTO> getProjectSprints(Integer memberId, Integer projectId) {
        // 验证用户和项目
        ProjectDTO project = projectService.getProject(memberId, projectId);
        if (project == null) {
            throw new RuntimeException("Project not found or user is not a member");
        }
        
        return sprintRepository.findByProjectId(projectId).stream()
                .map(this::convertToSprintDTO)
                .collect(Collectors.toList());
    }

    @Override
    @GlobalTransactional(timeoutMills = 30000)
    public SprintDTO createSprint(Integer memberId, Integer projectId, SprintCreateBody createBody) {
        try {
            // 验证用户是否是项目成员
            ProjectDTO project = projectService.getProject(memberId, projectId);
            if (project == null) {
                throw new RuntimeException("Project not found");
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
    public SprintDTO updateSprint(Integer memberId, Integer projectId, Integer sprintId, SprintUpdateBody updateBody) {
        // 验证用户和项目
        ProjectDTO project = projectService.getProject(memberId, projectId);
        if (project == null) {
            throw new RuntimeException("Project not found or user is not a member");
        }
        
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));
                
        if (!sprint.getProjectId().equals(projectId)) {
            throw new RuntimeException("Sprint does not belong to this project");
        }

        if (updateBody.getStatus() != null) {
            validateSprintStatusChange(sprint, updateBody.getStatus());
        }

        BeanCopyUtils.copyNonNullProperties(updateBody, sprint);
        return convertToSprintDTO(sprintRepository.save(sprint));
    }

    @Override
    @GlobalTransactional
    public void deleteSprint(Integer memberId, Integer projectId, Integer sprintId) {
        // 验证用户和项目
        ProjectDTO project = projectService.getProject(memberId, projectId);
        if (project == null) {
            throw new RuntimeException("Project not found or user is not a member");
        }
        
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));
                
        if (!sprint.getProjectId().equals(projectId)) {
            throw new RuntimeException("Sprint does not belong to this project");
        }
        
        sprintRepository.deleteById(sprintId);
    }

    // Task 相关操作
    @Override
    public TaskDTO getTaskById(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return convertToTaskDTO(task);
    }

    @Override
    public List<TaskDTO> getProjectTasks(Integer projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskDTO> getSprintTasks(Integer sprintId) {
        return taskRepository.findBySprintId(sprintId).stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskDTO createTask(TaskCreateBody createBody) {
        // 验证 sprint 是否存在
        Sprint sprint = null;
        if (createBody.getSprintId() != null) {
            sprint = sprintRepository.findById(createBody.getSprintId())
                    .orElseThrow(() -> new RuntimeException("Sprint not found"));
        }
        
        Task task = new Task();
        BeanUtils.copyProperties(createBody, task);
        task.setProjectId(createBody.getProjectId());
        task.setStatus("TO_DO");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setSprint(sprint);
        
        // 验证日期范围
        task.validateDates();
        
        return convertToTaskDTO(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskDTO updateTask(Integer taskId, TaskUpdateBody updateBody) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
            
        if (updateBody.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(updateBody.getSprintId())
                    .orElseThrow(() -> new RuntimeException("Sprint not found"));
            task.setSprint(sprint);
        }
        
        BeanCopyUtils.copyNonNullProperties(updateBody, task);
        task.setUpdatedAt(LocalDateTime.now());
        
        // 验证日期范围
        task.validateDates();
        
        return convertToTaskDTO(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void deleteTask(Integer taskId) {
        taskRepository.deleteById(taskId);
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
        
        // 如果需要计算故事点，确保 tasks 集合已初始化
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
        BeanUtils.copyProperties(task, dto);
        if (task.getSprint() != null) {
            dto.setSprintId(task.getSprint().getId());
        }
        return dto;
    }

//    private void validateProjectMember(Integer memberId, Integer projectId) {
//        // 通过 ProjectService 验证用户是否是项目成员
//        try {
//            projectService.getProject(memberId, projectId);
//        } catch (Exception e) {
//            throw new RuntimeException("Access denied: user is not a member of this project");
//        }
//    }

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
}
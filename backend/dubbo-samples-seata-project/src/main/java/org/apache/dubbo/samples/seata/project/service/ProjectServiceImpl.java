package org.apache.dubbo.samples.seata.project.service;

import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.samples.seata.api.ProjectService;
import org.apache.dubbo.samples.seata.api.dto.*;
import org.apache.dubbo.samples.seata.project.entity.Project;
import org.apache.dubbo.samples.seata.project.entity.Sprint;
import org.apache.dubbo.samples.seata.project.entity.Task;
import org.apache.dubbo.samples.seata.project.entity.Member;
import org.apache.dubbo.samples.seata.project.repository.ProjectRepository;
import org.apache.dubbo.samples.seata.project.repository.SprintRepository;
import org.apache.dubbo.samples.seata.project.repository.TaskRepository;
import org.apache.dubbo.samples.seata.project.util.BeanCopyUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@DubboService
public class ProjectServiceImpl implements ProjectService {
    
    @Resource
    private ProjectRepository projectRepository;
    
    @Resource
    private SprintRepository sprintRepository;
    
    @Resource
    private TaskRepository taskRepository;

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

    @Override
    @Transactional
    public ProjectDTO createProject(ProjectCreateBody createBody) {
        Project project = new Project();
        BeanUtils.copyProperties(createBody, project);
        project.setCreatedAt(LocalDateTime.now());
        project.setStatus("IN_PROGRESS");
        return convertToProjectDTO(projectRepository.save(project));
    }

    @Override
    @Transactional
    public ProjectDTO updateProject(Integer projectId, ProjectUpdateBody updateBody) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
            
        if (updateBody.getStatus() != null && updateBody.getStatus().equals("DONE")) {
            validateProjectCanBeCompleted(project);
        }

        BeanCopyUtils.copyNonNullProperties(updateBody, project);
        return convertToProjectDTO(projectRepository.save(project));
    }

    @Override
    @Transactional
    public void deleteProject(Integer projectId) {
        projectRepository.deleteById(projectId);
    }

    @Override
    public SprintDTO getSprintById(Integer sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
            .orElseThrow(() -> new RuntimeException("Sprint not found"));
        return convertToSprintDTO(sprint);
    }

    @Override
    public List<SprintDTO> getProjectSprints(Integer projectId) {
        return sprintRepository.findByProjectId(projectId).stream()
            .map(this::convertToSprintDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SprintDTO createSprint(SprintCreateBody createBody) {
        Project project = projectRepository.findById(createBody.getProjectId())
            .orElseThrow(() -> new RuntimeException("Project not found"));
            
        Sprint sprint = new Sprint();
        BeanUtils.copyProperties(createBody, sprint);
        sprint.setProject(project);
        sprint.setStatus("TO_DO");
        
        return convertToSprintDTO(sprintRepository.save(sprint));
    }

    @Override
    @Transactional
    public SprintDTO updateSprint(Integer sprintId, SprintUpdateBody updateBody) {
        Sprint sprint = sprintRepository.findById(sprintId)
            .orElseThrow(() -> new RuntimeException("Sprint not found"));
            
        if (updateBody.getStatus() != null) {
            validateSprintStatusChange(sprint, updateBody.getStatus());
        }
        
        BeanCopyUtils.copyNonNullProperties(updateBody, sprint);
        return convertToSprintDTO(sprintRepository.save(sprint));
    }

    @Override
    @Transactional
    public void deleteSprint(Integer sprintId) {
        sprintRepository.deleteById(sprintId);
    }

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
        Project project = projectRepository.findById(createBody.getProjectId())
            .orElseThrow(() -> new RuntimeException("Project not found"));
            
        Task task = new Task();
        BeanUtils.copyProperties(createBody, task);
        task.setProject(project);
        task.setStatus("TO_DO");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        if (createBody.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(createBody.getSprintId())
                .orElseThrow(() -> new RuntimeException("Sprint not found"));
            task.setSprint(sprint);
        }
        
        return convertToTaskDTO(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskDTO updateTask(Integer taskId, TaskUpdateBody updateBody) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
            
        if (updateBody.getStatus() != null) {
            validateTaskStatusChange(task, updateBody.getStatus());
        }
        
        BeanCopyUtils.copyNonNullProperties(updateBody, task);
        task.setUpdatedAt(LocalDateTime.now());
        
        if (updateBody.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(updateBody.getSprintId())
                .orElseThrow(() -> new RuntimeException("Sprint not found"));
            task.setSprint(sprint);
        }
        
        return convertToTaskDTO(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void deleteTask(Integer taskId) {
        taskRepository.deleteById(taskId);
    }

    @Override
    @Transactional
    public void addProjectMember(Integer projectId, Integer memberId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        Member member = new Member();
        member.setMemberId(memberId);
        project.getMembers().add(member);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public void removeProjectMember(Integer projectId, Integer memberId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        project.setMembers(project.getMembers().stream()
            .filter(member -> !member.getMemberId().equals(memberId))
            .collect(Collectors.toSet()));
        projectRepository.save(project);
    }

    @Override
    public List<MemberDTO> getProjectMembers(Integer projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        return project.getMembers().stream()
            .map(this::convertToMemberDTO)
            .collect(Collectors.toList());
    }

    private void validateProjectCanBeCompleted(Project project) {
        boolean hasUncompletedSprints = project.getSprints().stream()
            .anyMatch(sprint -> !sprint.getStatus().equals("DONE"));
        boolean hasUncompletedTasks = project.getTasks().stream()
            .anyMatch(task -> !task.getStatus().equals("DONE"));
            
        if (hasUncompletedSprints || hasUncompletedTasks) {
            throw new RuntimeException("Cannot complete project with uncompleted sprints or tasks");
        }
    }

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

    private void validateTaskStatusChange(Task task, String newStatus) {
        if (task.getSprint() != null && !task.getSprint().getStatus().equals("IN_PROGRESS")) {
            throw new RuntimeException("Cannot change task status when sprint is not in progress");
        }
    }

    // DTO conversion methods
    private ProjectDTO convertToProjectDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        BeanUtils.copyProperties(project, dto);
        return dto;
    }

    private SprintDTO convertToSprintDTO(Sprint sprint) {
        SprintDTO dto = new SprintDTO();
        BeanUtils.copyProperties(sprint, dto);
        dto.setProjectId(sprint.getProject().getId());
        dto.setTotalStoryPoints(sprint.getTotalStoryPoints());
        dto.setCompletedStoryPoints(sprint.getCompletedStoryPoints());
        return dto;
    }

    private TaskDTO convertToTaskDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        BeanUtils.copyProperties(task, dto);
        dto.setProjectId(task.getProject().getId());
        if (task.getSprint() != null) {
            dto.setSprintId(task.getSprint().getId());
        }
        return dto;
    }

    private MemberDTO convertToMemberDTO(Member member) {
        MemberDTO dto = new MemberDTO();
        dto.setMemberId(member.getMemberId());
        return dto;
    }
} 
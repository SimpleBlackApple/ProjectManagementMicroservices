package org.apache.dubbo.samples.seata.task.service;

import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.samples.seata.api.TaskService;
import org.apache.dubbo.samples.seata.api.dto.*;
import org.apache.dubbo.samples.seata.task.entity.Task;
import org.apache.dubbo.samples.seata.task.entity.Sprint;
import org.apache.dubbo.samples.seata.task.entity.Member;
import org.apache.dubbo.samples.seata.task.repository.TaskRepository;
import org.apache.dubbo.samples.seata.task.repository.SprintRepository;
import org.apache.dubbo.samples.seata.task.repository.MemberRepository;
import org.apache.dubbo.samples.seata.api.util.BeanCopyUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@DubboService
public class TaskServiceImpl implements TaskService {

    @Resource
    private SprintRepository sprintRepository;

    @Resource
    private TaskRepository taskRepository;

    @Resource
    private MemberRepository memberRepository;

    // Sprint 相关操作
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
        Sprint sprint = new Sprint();
        BeanUtils.copyProperties(createBody, sprint);
        sprint.setProjectId(createBody.getProjectId());
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
        Task task = new Task();
        BeanUtils.copyProperties(createBody, task);
        task.setProjectId(createBody.getProjectId());
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

    // Member 相关操作
    @Override
    @Transactional
    public void addProjectMember(Integer projectId, Integer memberId) {
        Member member = new Member();
        member.setMemberId(memberId);
        member.setProjectId(projectId);
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void removeProjectMember(Integer projectId, Integer memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        if (!projectId.equals(member.getProjectId())) {
            throw new RuntimeException("Member does not belong to this project");
        }
        memberRepository.deleteById(memberId);
    }

    @Override
    public List<MemberDTO> getProjectMembers(Integer projectId) {
        return memberRepository.findByProjectId(projectId).stream()
                .map(this::convertToMemberDTO)
                .collect(Collectors.toList());
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

    private void validateTaskStatusChange(Task task, String newStatus) {
        if (task.getSprint() != null && !task.getSprint().getStatus().equals("IN_PROGRESS")) {
            throw new RuntimeException("Cannot change task status when sprint is not in progress");
        }
    }

    private SprintDTO convertToSprintDTO(Sprint sprint) {
        SprintDTO dto = new SprintDTO();
        BeanUtils.copyProperties(sprint, dto);
        dto.setTotalStoryPoints(sprint.getTotalStoryPoints());
        dto.setCompletedStoryPoints(sprint.getCompletedStoryPoints());
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

    private MemberDTO convertToMemberDTO(Member member) {
        MemberDTO dto = new MemberDTO();
        BeanUtils.copyProperties(member, dto);
        return dto;
    }
}
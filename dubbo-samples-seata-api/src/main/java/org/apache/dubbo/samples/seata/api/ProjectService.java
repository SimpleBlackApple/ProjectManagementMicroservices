package org.apache.dubbo.samples.seata.api;

import org.apache.dubbo.samples.seata.api.dto.*;
import java.util.List;

public interface ProjectService {
    ProjectDTO getProjectById(Integer projectId);
    List<ProjectDTO> getAllProjects();
    ProjectDTO createProject(ProjectCreateBody projectCreateBody);
    ProjectDTO updateProject(Integer projectId, ProjectUpdateBody projectUpdateBody);
    void deleteProject(Integer projectId);
    
    SprintDTO getSprintById(Integer sprintId);
    List<SprintDTO> getProjectSprints(Integer projectId);
    SprintDTO createSprint(SprintCreateBody sprintCreateBody);
    SprintDTO updateSprint(Integer sprintId, SprintUpdateBody sprintUpdateBody);
    void deleteSprint(Integer sprintId);
    
    TaskDTO getTaskById(Integer taskId);
    List<TaskDTO> getProjectTasks(Integer projectId);
    List<TaskDTO> getSprintTasks(Integer sprintId);
    TaskDTO createTask(TaskCreateBody taskCreateBody);
    TaskDTO updateTask(Integer taskId, TaskUpdateBody taskUpdateBody);
    void deleteTask(Integer taskId);
    
    void addProjectMember(Integer projectId, Integer memberId);
    void removeProjectMember(Integer projectId, Integer memberId);
    List<MemberDTO> getProjectMembers(Integer projectId);
} 
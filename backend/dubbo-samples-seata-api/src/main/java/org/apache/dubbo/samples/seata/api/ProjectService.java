package org.apache.dubbo.samples.seata.api;

import org.apache.dubbo.samples.seata.api.dto.*;
import java.util.List;

public interface ProjectService {
    ProjectDTO getProjectById(Integer projectId);
    List<ProjectDTO> getAllProjects();
    ProjectDTO createProject(ProjectCreateBody createBody);
    ProjectDTO updateProject(Integer userId, Integer projectId, ProjectUpdateBody updateBody);
    void deleteProject(Integer projectId);
} 
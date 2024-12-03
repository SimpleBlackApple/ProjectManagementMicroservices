package org.apache.dubbo.samples.seata.task.service;

import org.apache.dubbo.samples.seata.api.dto.ProjectCreateBody;
import org.apache.dubbo.samples.seata.api.dto.ProjectDTO;

public interface TaskService {
    ProjectDTO createProjectWithOwner(ProjectCreateBody createBody);
    ProjectDTO updateProjectOwner(Integer projectId, Integer newOwnerId);
} 
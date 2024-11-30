package org.apache.dubbo.samples.seata.backlog.service;

import org.apache.dubbo.samples.seata.api.dto.ProjectCreateBody;
import org.apache.dubbo.samples.seata.api.dto.ProjectDTO;

public interface BacklogService {
    ProjectDTO createProjectWithOwner(ProjectCreateBody createBody);
    ProjectDTO updateProjectOwner(Integer projectId, Integer newOwnerId);
} 
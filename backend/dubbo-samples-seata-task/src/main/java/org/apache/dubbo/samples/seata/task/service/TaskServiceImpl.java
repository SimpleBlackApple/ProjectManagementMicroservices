package org.apache.dubbo.samples.seata.task.service;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.samples.seata.api.ProjectService;
import org.apache.dubbo.samples.seata.api.UserService;
import org.apache.dubbo.samples.seata.api.dto.ProjectCreateBody;
import org.apache.dubbo.samples.seata.api.dto.ProjectDTO;
import org.apache.dubbo.samples.seata.api.dto.ProjectUpdateBody;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl implements TaskService {
    
    @DubboReference(check = false)
    private ProjectService projectService;
    
    @DubboReference(check = false)
    private UserService userService;
    
    @Override
    @GlobalTransactional(timeoutMills = 300000, name = "dubbo-samples-seata")
    public ProjectDTO createProjectWithOwner(ProjectCreateBody createBody) {
        // 验证owner是否存在
        userService.getUserById(createBody.getOwnerId());
        // 创建项目
        return projectService.createProject(createBody);
    }
    
    @Override
    @GlobalTransactional(timeoutMills = 300000, name = "dubbo-samples-seata")
    public ProjectDTO updateProjectOwner(Integer projectId, Integer newOwnerId) {
        // 验证新owner是否存在
        userService.getUserById(newOwnerId);
        
        // 更新项目owner
        ProjectUpdateBody updateBody = new ProjectUpdateBody();
        updateBody.setOwnerId(newOwnerId);
        return projectService.updateProject(projectId, updateBody);
    }
} 
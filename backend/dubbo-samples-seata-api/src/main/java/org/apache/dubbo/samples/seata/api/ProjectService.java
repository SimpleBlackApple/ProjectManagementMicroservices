package org.apache.dubbo.samples.seata.api;

import org.apache.dubbo.samples.seata.api.dto.*;
import java.util.List;

public interface ProjectService {
    List<ProjectDTO> getProjectByOwnerId(Integer ownerId);
    List<ProjectDTO> getAllProjects();
    ProjectDTO createProject(Integer ownerId, ProjectCreateBody createBody);
    ProjectDTO updateProject(Integer memberId, Integer projectId, ProjectUpdateBody updateBody);
    void deleteProject(Integer memberId, Integer projectId);
    ProjectDTO addMember(Integer ownerId, Integer projectId, Integer newUserId);
    void removeMember(Integer ownerId, Integer projectId, Integer memberId);
    List<MemberDTO> getProjectMembers(Integer memberId, Integer projectId);
    ProjectDTO getProject(Integer memberId, Integer projectId);
    void handleUserDeletion(Integer userId);
    boolean isUserProjectOwner(Integer userId);
} 
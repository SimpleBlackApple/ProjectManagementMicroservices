package org.apache.dubbo.samples.seata.api;

import org.apache.dubbo.samples.seata.api.dto.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface ProjectService {
    List<ProjectDTO> getProjectByOwner(UserDetails owner);
    List<ProjectDTO> getAllProjects(UserDetails user);
    ProjectDTO createProject(UserDetails owner, ProjectCreateBody createBody);
    ProjectDTO updateProject(UserDetails user, Integer projectId, ProjectUpdateBody updateBody);
    void deleteProject(UserDetails user, Integer projectId);
    ProjectDTO addMember(UserDetails owner, Integer projectId, Integer newUserId);
    void removeMember(UserDetails owner, Integer projectId, Integer memberId);
    List<MemberDTO> getProjectMembers(UserDetails user, Integer projectId);
    ProjectDTO getProject(UserDetails user, Integer projectId);
    void handleUserDeletion(Integer userId);
    boolean isUserProjectOwner(Integer userId);
    void deleteProjectRollback(UserDetails user, Integer projectId);
    void syncNewUser(Integer userId, String email);
    void removeUserData(Integer userId);
    ProjectDTO transferOwnership(UserDetails currentOwner, Integer projectId, Integer newOwnerId);
} 
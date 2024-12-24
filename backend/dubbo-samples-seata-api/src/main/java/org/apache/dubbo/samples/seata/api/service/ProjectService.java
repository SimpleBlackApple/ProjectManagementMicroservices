package org.apache.dubbo.samples.seata.api.service;

import org.apache.dubbo.samples.seata.api.dto.*;

import java.util.List;

public interface ProjectService {
    List<ProjectDTO> getProjectByOwner(String email);
    List<ProjectDTO> getAllProjects(String email);
    ProjectDTO createProject(String email, ProjectCreateBody createBody);
    ProjectDTO updateProject(String email, Integer projectId, ProjectUpdateBody updateBody);
    void deleteProject(String email, Integer projectId);
    ProjectDTO addMember(String email, Integer projectId, String newUserEmail);
    void removeMember(String email, Integer projectId, String memberEmail);
    List<MemberDTO> getProjectMembers(String email, Integer projectId);
    ProjectDTO getProject(String email, Integer projectId);
    void handleUserDeletion(String email);
    boolean isUserProjectOwner(String email);
    void deleteProjectRollback(String email, Integer projectId);
    void syncNewUser(Integer userId, String name, String email, String password);
    void removeUserData(String email);
    ProjectDTO transferOwnership(String currentOwnerEmail, Integer projectId, String newOwnerEmail);
    boolean validateUserProject(String email, Integer projectId);
} 
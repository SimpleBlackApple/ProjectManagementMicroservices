package org.apache.dubbo.samples.seata.project;

import org.apache.dubbo.samples.seata.api.ProjectService;
import org.apache.dubbo.samples.seata.api.dto.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ProjectApplicationIT {
    
    private final RestTemplate restTemplate;
    private final String USER_API_BASE_URL = "http://localhost:8081/api/users";

    @Autowired
    private ProjectService projectService;

    private Integer ownerUserId;
    private Integer memberUserId;

    public ProjectApplicationIT() {
        this.restTemplate = new RestTemplateBuilder().build();
    }

    @BeforeEach
    void setUp() {
        // 通过REST API创建两个测试用户
        UserCreateBody ownerUser = new UserCreateBody();
        ownerUser.setUsername("test_owner");
        ownerUser.setEmail("owner@test.com");
        ownerUser.setPasswordHash("test123");

        UserCreateBody memberUser = new UserCreateBody();
        memberUser.setUsername("test_member");
        memberUser.setEmail("member@test.com");
        memberUser.setPasswordHash("test123");

        ResponseEntity<UserDTO> ownerResponse = restTemplate.postForEntity(
            USER_API_BASE_URL,
            ownerUser,
            UserDTO.class
        );
        
        ResponseEntity<UserDTO> memberResponse = restTemplate.postForEntity(
            USER_API_BASE_URL,
            memberUser,
            UserDTO.class
        );

        ownerUserId = ownerResponse.getBody().getUserId();
        memberUserId = memberResponse.getBody().getUserId();
    }

    @AfterEach
    void tearDown() {
        // 清理创建的测试用户
        if (ownerUserId != null) {
            restTemplate.delete(USER_API_BASE_URL + "/{id}", ownerUserId);
        }
        if (memberUserId != null) {
            restTemplate.delete(USER_API_BASE_URL + "/{id}", memberUserId);
        }
    }

    @Test
    void testProjectLifecycle() {
        // 1. 测试创建项目
        ProjectCreateBody createBody = new ProjectCreateBody();
        createBody.setName("Test Project");
        createBody.setDescription("Test Description");
        
        ProjectDTO createdProject = projectService.createProject(ownerUserId, createBody);
        
        assertNotNull(createdProject);
        assertEquals("Test Project", createdProject.getName());
        assertEquals("Test Description", createdProject.getDescription());
        assertEquals(ownerUserId, createdProject.getOwnerId());
        
        // 2. 测试添加成员
        ProjectDTO projectWithNewMember = projectService.addMember(
            ownerUserId, 
            createdProject.getId(), 
            memberUserId
        );
        
        assertNotNull(projectWithNewMember);
        assertTrue(projectWithNewMember.getMembers().stream()
            .anyMatch(member -> member.getUserId().equals(memberUserId)));
            
        // 3. 测试成员更新项目
        ProjectUpdateBody updateBody = new ProjectUpdateBody();
        updateBody.setName("Updated Project");
        updateBody.setDescription("Updated Description");
        
        ProjectDTO updatedProject = projectService.updateProject(
            memberUserId,
            createdProject.getId(),
            updateBody
        );
        
        assertNotNull(updatedProject);
        assertEquals("Updated Project", updatedProject.getName());
        assertEquals("Updated Description", updatedProject.getDescription());
    }

    @Test
    void testProjectAccessControl() {
        // 1. 创建测试项目
        ProjectCreateBody createBody = new ProjectCreateBody();
        createBody.setName("Access Control Test");
        ProjectDTO project = projectService.createProject(ownerUserId, createBody);

        // 2. 测试非成员无法更新项目
        ProjectUpdateBody updateBody = new ProjectUpdateBody();
        updateBody.setName("Unauthorized Update");

        assertThrows(RuntimeException.class, () ->
            projectService.updateProject(memberUserId, project.getId(), updateBody)
        );

        // 3. 测试非owner无法添加成员
        assertThrows(RuntimeException.class, () ->
            projectService.addMember(memberUserId, project.getId(), memberUserId)
        );
    }
} 
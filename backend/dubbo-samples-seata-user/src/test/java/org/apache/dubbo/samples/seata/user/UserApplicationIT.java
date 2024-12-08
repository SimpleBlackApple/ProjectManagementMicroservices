package org.apache.dubbo.samples.seata.user;

import org.apache.dubbo.samples.seata.api.UserService;
import org.apache.dubbo.samples.seata.api.dto.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserApplicationIT {
    
    @Autowired
    private UserService userService;
    
    private final RestTemplate restTemplate;
    private final String USER_API_BASE_URL = "http://localhost:8081/api/users";
    private final String PROJECT_API_BASE_URL = "http://localhost:8082/api/projects";
    
    private Integer ownerUserId;
    private Integer member1UserId;
    private Integer member2UserId;
    private Integer projectId;

    public UserApplicationIT() {
        this.restTemplate = new RestTemplateBuilder().build();
    }

    @BeforeEach
    void setUp() {
        // 创建个测试用户
        UserCreateBody ownerUser = new UserCreateBody();
        ownerUser.setUsername("test_owner");
        ownerUser.setEmail("owner@test.com");
        ownerUser.setPasswordHash("test123");

        UserCreateBody member1User = new UserCreateBody();
        member1User.setUsername("test_member1");
        member1User.setEmail("member1@test.com");
        member1User.setPasswordHash("test123");

        UserCreateBody member2User = new UserCreateBody();
        member2User.setUsername("test_member2");
        member2User.setEmail("member2@test.com");
        member2User.setPasswordHash("test123");

        ResponseEntity<UserDTO> ownerResponse = restTemplate.postForEntity(
            USER_API_BASE_URL,
            ownerUser,
            UserDTO.class
        );
        ownerUserId = ownerResponse.getBody().getUserId();

        ResponseEntity<UserDTO> member1Response = restTemplate.postForEntity(
            USER_API_BASE_URL,
            member1User,
            UserDTO.class
        );
        member1UserId = member1Response.getBody().getUserId();

        ResponseEntity<UserDTO> member2Response = restTemplate.postForEntity(
            USER_API_BASE_URL,
            member2User,
            UserDTO.class
        );
        member2UserId = member2Response.getBody().getUserId();
        
        // 创建测试项目
        ProjectCreateBody projectCreateBody = new ProjectCreateBody();
        projectCreateBody.setName("Test Project");
        projectCreateBody.setDescription("Test Project Description");
        
        ResponseEntity<ProjectDTO> projectResponse = restTemplate.postForEntity(
            PROJECT_API_BASE_URL + "/{ownerId}",
            projectCreateBody,
            ProjectDTO.class,
            ownerUserId
        );
        
        projectId = projectResponse.getBody().getId();
        
        // 添加两个成员
        restTemplate.postForEntity(
            PROJECT_API_BASE_URL + "/{ownerId}/{projectId}/members/{newUserId}",
            null,
            ProjectDTO.class,
            ownerUserId,
            projectId,
            member1UserId
        );
        
        restTemplate.postForEntity(
            PROJECT_API_BASE_URL + "/{ownerId}/{projectId}/members/{newUserId}",
            null,
            ProjectDTO.class,
            ownerUserId,
            projectId,
            member2UserId
        );
    }

    @AfterEach
    void tearDown() {
        // 删除项目
        if (projectId != null) {
            try {
                // 先获取项目信息，确定当前的 owner
                ResponseEntity<ProjectDTO> projectResponse = restTemplate.getForEntity(
                    PROJECT_API_BASE_URL + "/{memberId}/{projectId}",
                    ProjectDTO.class,
                    member1UserId, // 使用 member1 来查询，因为它是最早加入的成员
                    projectId
                );
                
                // 使用当前的 owner 删除项目
                Integer currentOwnerId = projectResponse.getBody().getOwnerId();
                restTemplate.delete(
                    PROJECT_API_BASE_URL + "/{ownerId}/{projectId}",
                    currentOwnerId,
                    projectId
                );
            } catch (Exception e) {
                // 如果获取项目信息失败，尝试使用原始 owner 删除
                if (ownerUserId != null) {
                    restTemplate.delete(
                        PROJECT_API_BASE_URL + "/{ownerId}/{projectId}",
                        ownerUserId,
                        projectId
                    );
                }
            }
        }

        // 删除用户
        if (member2UserId != null) {
            restTemplate.delete(USER_API_BASE_URL + "/{id}/force", member2UserId);
        }
        if (member1UserId != null) {
            restTemplate.delete(USER_API_BASE_URL + "/{id}/force", member1UserId);
        }
        if (ownerUserId != null) {
            restTemplate.delete(USER_API_BASE_URL + "/{id}/force", ownerUserId);
        }
    }

    @Test
    void testUserDeletionScenario() {
        // 1. 尝试普通删除项目所有者（应该失败）
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.Forbidden.class, () -> {
            restTemplate.delete(USER_API_BASE_URL + "/{id}", ownerUserId);
        });
        assertTrue(exception.getResponseBodyAsString().contains("Cannot delete user who owns projects"));

        // 2. 获取删除前的项目成员信息
        List<Map<String, Object>> beforeDeleteMembers = restTemplate.getForObject(
            PROJECT_API_BASE_URL + "/{memberId}/{projectId}/members",
            List.class,
            member1UserId,
            projectId
        );
        
        assertEquals(3, beforeDeleteMembers.size());
        assertTrue(beforeDeleteMembers.stream()
            .anyMatch(m -> m.get("userId").equals(ownerUserId) && !(Boolean)m.get("deleted")));

        // 3. 强制删除项目所有者
        restTemplate.delete(USER_API_BASE_URL + "/{id}/force", ownerUserId);

        // 4. 验证项目仍然存在，但所有权已转移给最早加入的成员
        ResponseEntity<ProjectDTO> afterDeleteProject = restTemplate.getForEntity(
            PROJECT_API_BASE_URL + "/{memberId}/{projectId}",
            ProjectDTO.class,
            member1UserId,
            projectId
        );
        
        ProjectDTO updatedProject = afterDeleteProject.getBody();
        assertEquals(member1UserId, updatedProject.getOwnerId());
        
        // 5. 验证成员列表中原所有者已标记为删除
        List<Map<String, Object>> afterDeleteMembers = restTemplate.getForObject(
            PROJECT_API_BASE_URL + "/{memberId}/{projectId}/members",
            List.class,
            member1UserId,
            projectId
        );
        
        assertEquals(3, afterDeleteMembers.size());  // 总成员数应该保持不变
        assertTrue(afterDeleteMembers.stream()
            .anyMatch(m -> m.get("userId").equals(ownerUserId) && (Boolean)m.get("deleted")));

        // 6. 验证无法再获取已删除用户的信息
        HttpClientErrorException userNotFoundException = assertThrows(HttpClientErrorException.NotFound.class, () -> {
            restTemplate.getForEntity(
                USER_API_BASE_URL + "/{id}",
                UserDTO.class,
                ownerUserId
            );
        });
        assertTrue(userNotFoundException.getResponseBodyAsString().contains("User not found"));
        
        // 设置为null避免tearDown重复删除
        ownerUserId = null;
    }

    @Test
    void testNormalMemberDeletion() {
        // 1. 获取删除前的成员信息
        List<Map<String, Object>> beforeDeleteMembers = restTemplate.getForObject(
            PROJECT_API_BASE_URL + "/{memberId}/{projectId}/members",
            List.class,
            ownerUserId,
            projectId
        );
        
        assertEquals(3, beforeDeleteMembers.size());
        assertTrue(beforeDeleteMembers.stream()
            .anyMatch(m -> m.get("userId").equals(member2UserId) && !(Boolean)m.get("deleted")));

        // 2. 测试删除普通成员
        restTemplate.delete(USER_API_BASE_URL + "/{id}", member2UserId);

        // 3. 验证项目所有权未发生变化
        ResponseEntity<ProjectDTO> projectResponse = restTemplate.getForEntity(
            PROJECT_API_BASE_URL + "/{memberId}/{projectId}",
            ProjectDTO.class,
            member1UserId,
            projectId
        );
        
        assertEquals(ownerUserId, projectResponse.getBody().getOwnerId());
        
        // 4. 验证成员已被标记为删除
        List<Map<String, Object>> afterDeleteMembers = restTemplate.getForObject(
            PROJECT_API_BASE_URL + "/{memberId}/{projectId}/members",
            List.class,
            ownerUserId,
            projectId
        );
        
        assertEquals(3, afterDeleteMembers.size());  // 总成员数应该保持不变
        assertTrue(afterDeleteMembers.stream()
            .anyMatch(m -> m.get("userId").equals(member2UserId) && (Boolean)m.get("deleted")));

        // 5. 验证无法再获取已删除用户的信息
        HttpClientErrorException userNotFoundException = assertThrows(HttpClientErrorException.NotFound.class, () -> {
            restTemplate.getForEntity(
                USER_API_BASE_URL + "/{id}",
                UserDTO.class,
                member2UserId
            );
        });
        assertTrue(userNotFoundException.getResponseBodyAsString().contains("User not found"));
        
        // 设置为null避免tearDown重复删除
        member2UserId = null;
    }

    @Test
    void testForceDeleteOwnerRollback() {
        // 1. 验证初始状态
        UserDTO ownerUserBefore = userService.getUserById(ownerUserId);
        assertNotNull(ownerUserBefore);
        
        // 使用 REST API 检查项目所有权
        ResponseEntity<ProjectDTO> projectResponse = restTemplate.getForEntity(
            PROJECT_API_BASE_URL + "/{memberId}/{projectId}",
            ProjectDTO.class,
            ownerUserId,
            projectId
        );
        assertEquals(ownerUserId, projectResponse.getBody().getOwnerId());
        
        // 2. 尝试删除所有者，这会触发回滚
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteUserRollback(ownerUserId);
        });
        assertTrue(exception.getMessage().contains("Simulated error for testing rollback"));
        
        // 3. 验证由于事务回滚，所有数据应该保持不变
        UserDTO ownerUserAfter = userService.getUserById(ownerUserId);
        assertNotNull(ownerUserAfter);
        assertEquals(ownerUserBefore.getUserId(), ownerUserAfter.getUserId());
        assertEquals(ownerUserBefore.getUsername(), ownerUserAfter.getUsername());
        
        // 验证项目所有权未变
        ResponseEntity<ProjectDTO> afterProjectResponse = restTemplate.getForEntity(
            PROJECT_API_BASE_URL + "/{memberId}/{projectId}",
            ProjectDTO.class,
            ownerUserId,
            projectId
        );
        assertEquals(ownerUserId, afterProjectResponse.getBody().getOwnerId());
        
        // 验证项目成员关系未变
        List<Map<String, Object>> membersAfter = restTemplate.getForObject(
            PROJECT_API_BASE_URL + "/{memberId}/{projectId}/members",
            List.class,
            ownerUserId,
            projectId
        );
        assertEquals(3, membersAfter.size());  // 应该还是3个成员
        assertFalse(membersAfter.stream()
            .anyMatch(m -> m.get("userId").equals(ownerUserId) && (Boolean) m.get("deleted")));  // 所有者不应该被标记为删除
    }
} 
package org.apache.dubbo.samples.seata.task;

import org.apache.dubbo.samples.seata.api.TaskService;
import org.apache.dubbo.samples.seata.api.ProjectService;
import org.apache.dubbo.samples.seata.api.dto.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TaskApplicationIT {
    
    private final RestTemplate restTemplate;
    private final String USER_API_BASE_URL = "http://localhost:8081/api/users";
    private final String PROJECT_API_BASE_URL = "http://localhost:8082/api/projects";

    @Autowired
    private TaskService taskService;

    private Integer ownerUserId;
    private Integer memberUserId;
    private Integer projectId;

    public TaskApplicationIT() {
        this.restTemplate = new RestTemplateBuilder().build();
    }

    @BeforeEach
    void setUp() {
        // 创建测试用户
        UserCreateBody ownerUser = new UserCreateBody();
        ownerUser.setUsername("test_owner99");
        ownerUser.setEmail("owner99@test.com");
        ownerUser.setPasswordHash("test123");

        UserCreateBody memberUser = new UserCreateBody();
        memberUser.setUsername("test_member99");
        memberUser.setEmail("member99@test.com");
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

        ownerUserId = ownerResponse.getBody().getId();
        memberUserId = memberResponse.getBody().getId();

        // 创建测试项目
        ProjectCreateBody projectCreateBody = new ProjectCreateBody();
        projectCreateBody.setName("Test Project");
        projectCreateBody.setDescription("Test Description");

        ResponseEntity<ProjectDTO> projectResponse = restTemplate.postForEntity(
            PROJECT_API_BASE_URL + "/{ownerId}",
            projectCreateBody,
            ProjectDTO.class,
            ownerUserId
        );

        projectId = projectResponse.getBody().getId();

        // 添加成员到项目
        restTemplate.postForEntity(
            PROJECT_API_BASE_URL + "/{ownerId}/{projectId}/members/{newUserId}",
            null,
            ProjectDTO.class,
            ownerUserId,
            projectId,
            memberUserId
        );
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        if (projectId != null) {
            try {
                restTemplate.delete(
                    PROJECT_API_BASE_URL + "/{memberId}/{projectId}",
                    ownerUserId,
                    projectId
                );
            } catch (Exception e) {
                // 如果是因为项目已经被删除而报错，则忽略
                if (!e.getMessage().contains("Project not found")) {
                    throw new RuntimeException("Fail to delete project in teardown: " + e.getMessage());
                }
            }
        }

        if (ownerUserId != null) {
            restTemplate.delete(USER_API_BASE_URL + "/{id}/force", ownerUserId);
        }
        if (memberUserId != null) {
            restTemplate.delete(USER_API_BASE_URL + "/{id}/force", memberUserId);
        }
    }

    @Test
    void testSprintLifecycle() {
        // 1. 创建 Sprint
        SprintCreateBody createBody = new SprintCreateBody();
        createBody.setName("Test Sprint");
        createBody.setStartDate(LocalDateTime.now().plusDays(1));
        createBody.setEndDate(LocalDateTime.now().plusDays(14));
        
        SprintDTO createdSprint = taskService.createSprint(ownerUserId, projectId, createBody);
        
        assertNotNull(createdSprint);
        assertEquals("Test Sprint", createdSprint.getName());
        
        // 2. 更新 Sprint
        SprintUpdateBody updateBody = new SprintUpdateBody();
        updateBody.setName("Updated Sprint");
        
        SprintDTO updatedSprint = taskService.updateSprint(
            ownerUserId,
            projectId,
            createdSprint.getId(),
            updateBody
        );
        
        assertNotNull(updatedSprint);
        assertEquals("Updated Sprint", updatedSprint.getName());
        
        // 3. 删除 Sprint
        assertDoesNotThrow(() -> 
            taskService.deleteSprint(ownerUserId, projectId, createdSprint.getId())
        );
    }

    @Test
    void testTaskLifecycle() {
        // 1. 创建 Task
        TaskCreateBody createBody = new TaskCreateBody();
        createBody.setTitle("Test Task");
        createBody.setDescription("Test Description");
        createBody.setManagerId(memberUserId);  // 指定任务负责人
        createBody.setStartDate(LocalDateTime.now().plusDays(1));
        createBody.setDueDate(LocalDateTime.now().plusDays(7));

        TaskDTO createdTask = taskService.createTask(ownerUserId, projectId, createBody);

        assertNotNull(createdTask);
        assertEquals("Test Task", createdTask.getTitle());
        assertEquals(memberUserId, createdTask.getManagerId());

        // 2. 更新 Task
        TaskUpdateBody updateBody = new TaskUpdateBody();
        updateBody.setTitle("Updated Task");
        updateBody.setStatus("IN_PROGRESS");

        TaskDTO updatedTask = taskService.updateTask(
            memberUserId,  // 只有任务负责人可以更新任务
            createdTask.getId(),
            updateBody
        );

        assertNotNull(updatedTask);
        assertEquals("Updated Task", updatedTask.getTitle());
        assertEquals("IN_PROGRESS", updatedTask.getStatus());

        // 3. 删除 Task
        assertDoesNotThrow(() ->
            taskService.deleteTask(memberUserId, createdTask.getId())
        );
    }

    @Test
    void testDeleteProjectWithTasksAndSprints() {
        // 1. 创建 Sprint
        SprintCreateBody sprintCreateBody = new SprintCreateBody();
        sprintCreateBody.setName("Sprint to Delete");
        sprintCreateBody.setStartDate(LocalDateTime.now().plusDays(1));
        sprintCreateBody.setEndDate(LocalDateTime.now().plusDays(14));
        
        SprintDTO sprint = taskService.createSprint(ownerUserId, projectId, sprintCreateBody);
        
        // 2. 创建 Task
        TaskCreateBody taskCreateBody = new TaskCreateBody();
        taskCreateBody.setTitle("Task to Delete");
        taskCreateBody.setManagerId(memberUserId);
        taskCreateBody.setSprintId(sprint.getId());
        taskCreateBody.setStartDate(LocalDateTime.now().plusDays(1));
        taskCreateBody.setDueDate(LocalDateTime.now().plusDays(7));
        
        TaskDTO task = taskService.createTask(ownerUserId, projectId, taskCreateBody);

        // 3. 删除项目并验证关联数据也被删除
        restTemplate.delete(
                PROJECT_API_BASE_URL + "/{memberId}/{projectId}",
                ownerUserId,
                projectId
        );

        // 4. 验证 Sprint 和 Task 已被删除
        assertThrows(RuntimeException.class, () ->
            taskService.getSprintById(ownerUserId, projectId, sprint.getId())
        );
        
        assertThrows(RuntimeException.class, () ->
            taskService.getTaskById(ownerUserId, task.getId())
        );
    }
} 
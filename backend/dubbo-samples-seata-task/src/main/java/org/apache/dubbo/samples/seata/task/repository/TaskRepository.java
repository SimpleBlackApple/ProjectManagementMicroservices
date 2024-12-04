package org.apache.dubbo.samples.seata.task.repository;

import org.apache.dubbo.samples.seata.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByProjectId(Integer projectId);
    List<Task> findBySprintId(Integer sprintId);
    List<Task> findByAssignedMemberMemberId(Integer memberId);
    void deleteByProjectId(Integer projectId);
}
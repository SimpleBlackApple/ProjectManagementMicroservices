package org.apache.dubbo.samples.seata.task.repository;

import org.apache.dubbo.samples.seata.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByProjectId(Integer projectId);
    List<Task> findBySprintId(Integer sprintId);
    List<Task> findBySprintIdAndStatus(Integer sprintId, String status);
    List<Task> findByMemberId(Integer memberId);
    void deleteByProjectId(Integer projectId);
}
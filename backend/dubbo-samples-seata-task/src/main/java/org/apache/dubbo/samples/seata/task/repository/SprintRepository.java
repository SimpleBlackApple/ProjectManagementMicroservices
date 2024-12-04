package org.apache.dubbo.samples.seata.task.repository;

import org.apache.dubbo.samples.seata.task.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SprintRepository extends JpaRepository<Sprint, Integer> {
    List<Sprint> findByProjectId(Integer projectId);
    void deleteByProjectId(Integer projectId);
}
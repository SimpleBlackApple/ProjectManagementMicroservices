package org.apache.dubbo.samples.seata.task.repository;

import org.apache.dubbo.samples.seata.task.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Integer> {
    List<Sprint> findByProjectId(Integer projectId);

    void deleteByProjectId(Integer projectId);
}
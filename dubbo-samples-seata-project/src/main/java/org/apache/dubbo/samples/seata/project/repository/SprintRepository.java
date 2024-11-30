package org.apache.dubbo.samples.seata.project.repository;

import org.apache.dubbo.samples.seata.project.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SprintRepository extends JpaRepository<Sprint, Integer> {
    List<Sprint> findByProjectId(Integer projectId);
} 
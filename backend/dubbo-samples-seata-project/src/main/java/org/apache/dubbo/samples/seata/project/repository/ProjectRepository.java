package org.apache.dubbo.samples.seata.project.repository;

import org.apache.dubbo.samples.seata.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    List<Project> findByOwnerId(Integer ownerId);
}
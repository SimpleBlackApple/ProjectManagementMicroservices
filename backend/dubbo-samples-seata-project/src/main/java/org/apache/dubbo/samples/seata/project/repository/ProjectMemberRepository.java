package org.apache.dubbo.samples.seata.project.repository;

import org.apache.dubbo.samples.seata.project.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {
    boolean existsByProjectIdAndUserId(Integer projectId, Integer userId);
    List<ProjectMember> findByProjectId(Integer projectId);
}
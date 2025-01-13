package org.apache.dubbo.samples.seata.project.repository;

import java.util.List;
import java.util.Optional;

import org.apache.dubbo.samples.seata.project.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {

    boolean existsByProjectIdAndUserId(Integer projectId, Integer userId);

    List<ProjectMember> findByProjectId(Integer projectId);

    List<ProjectMember> findByUserIdAndDeletedFalseOrderByJoinedAtAsc(Integer userId);

    List<ProjectMember> findByProjectIdAndDeletedFalseOrderByJoinedAtAsc(Integer projectId);

    boolean existsByProjectIdAndUserIdAndDeletedFalse(Integer projectId, Integer userId);

    void deleteByProjectId(Integer projectId);

    Optional<ProjectMember> findByProjectIdAndUserId(Integer projectId, Integer userId);
}

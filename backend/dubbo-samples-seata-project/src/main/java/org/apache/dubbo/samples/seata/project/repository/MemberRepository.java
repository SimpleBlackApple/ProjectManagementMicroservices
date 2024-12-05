package org.apache.dubbo.samples.seata.project.repository;

import org.apache.dubbo.samples.seata.project.entity.Member;
import org.apache.dubbo.samples.seata.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findByUserId(Integer userId);
    Optional<Member> findByUserIdAndProjectsContaining(Integer userId, Project project);
    List<Member> findByProjects_Id(Integer projectId);
} 
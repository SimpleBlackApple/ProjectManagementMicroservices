package org.apache.dubbo.samples.seata.task.repository;

import org.apache.dubbo.samples.seata.task.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    List<Member> findByProjectId(Integer projectId);
    List<Member> findBySprintsId(Integer sprintId);
    void deleteByProjectId(Integer projectId);
}
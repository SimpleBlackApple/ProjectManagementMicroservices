package org.apache.dubbo.samples.seata.task.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Table(name = "member")
public class Member {
    @Id
    private Integer memberId;
    
    @Column(name = "project_id")
    private Integer projectId;
    
    @OneToOne
    @JoinColumn(name = "task_id")
    private Task assignedTask;
    
    @ManyToMany
    @JoinTable(
        name = "member_sprint",
        joinColumns = @JoinColumn(name = "member_id"),
        inverseJoinColumns = @JoinColumn(name = "sprint_id")
    )
    private Set<Sprint> sprints;
} 
package org.apache.dubbo.samples.seata.project.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Table(name = "member")
public class Member {
    @Id
    private Integer memberId;
    
    @ManyToMany(mappedBy = "members")
    private Set<Project> projects;
} 
package org.apache.dubbo.samples.seata.project.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Getter
@Setter
@Entity
@Table(name = "projects")
@ToString(exclude = "members")
@EqualsAndHashCode(exclude = "members")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String name;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private Integer ownerId;

    @ManyToMany
    @JoinTable(
        name = "project_members",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private Set<Member> members = new HashSet<>();
}
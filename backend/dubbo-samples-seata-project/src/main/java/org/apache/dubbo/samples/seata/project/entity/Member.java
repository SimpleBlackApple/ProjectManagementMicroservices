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
@Table(name = "members")
@ToString(exclude = "projects")
@EqualsAndHashCode(exclude = "projects")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;
    private LocalDateTime joinedAt;

    @ManyToMany(mappedBy = "members")
    private Set<Project> projects = new HashSet<>();
} 
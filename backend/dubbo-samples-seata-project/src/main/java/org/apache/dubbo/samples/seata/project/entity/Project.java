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
@ToString(exclude = "projectMembers")
@EqualsAndHashCode(exclude = "projectMembers")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String name;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private Integer ownerId;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMember> projectMembers = new HashSet<>();

    public void addMember(Integer userId) {
        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(this);
        projectMember.setUserId(userId);
        projectMember.setJoinedAt(LocalDateTime.now());
        this.projectMembers.add(projectMember);
    }

    public void removeMember(Integer userId) {
        this.projectMembers.removeIf(member -> member.getUserId().equals(userId));
    }
}
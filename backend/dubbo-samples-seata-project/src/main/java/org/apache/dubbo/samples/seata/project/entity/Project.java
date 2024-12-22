package org.apache.dubbo.samples.seata.project.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.userdetails.UserDetails;

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

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMember> projectMembers = new HashSet<>();

    public void setOwner(UserDetails owner) {
        this.owner = (User) owner;
    }

    public void addMember(UserDetails user) {
        // 检查是否已经是成员
        boolean isMember = this.projectMembers.stream()
                .anyMatch(member -> member.getUser().equals(user) && !member.isDeleted());
        
        if (isMember) {
            throw new RuntimeException("User is already a member of this project");
        }
        
        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(this);
        projectMember.setUser((User) user);
        projectMember.setJoinedAt(LocalDateTime.now());
        this.projectMembers.add(projectMember);
    }

    public void removeMember(UserDetails user) {
        this.projectMembers.removeIf(member -> member.getUser().equals(user));
    }
}
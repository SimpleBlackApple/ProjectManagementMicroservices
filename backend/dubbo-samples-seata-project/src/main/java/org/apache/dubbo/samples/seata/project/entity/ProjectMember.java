package org.apache.dubbo.samples.seata.project.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_members")
@Getter
@Setter
@NoArgsConstructor
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "user_id")
    private Integer userId;

    private LocalDateTime joinedAt;
} 
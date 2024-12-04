package org.apache.dubbo.samples.seata.task.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String title;
    private String description;
    private String type;
    private String status;
    private Integer storyPoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dueDate;
    
    @Column(name = "project_id", nullable = false)
    private Integer projectId;
    
    @ManyToOne
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;
    
    @OneToOne(mappedBy = "assignedTask")
    private Member assignedMember;
} 
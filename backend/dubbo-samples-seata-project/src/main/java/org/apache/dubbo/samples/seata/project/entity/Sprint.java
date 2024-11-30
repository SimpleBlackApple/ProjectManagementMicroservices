package org.apache.dubbo.samples.seata.project.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "sprints")
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    
    @OneToMany(mappedBy = "sprint")
    private Set<Task> tasks;
    
    @Transient
    public Integer getTotalStoryPoints() {
        return tasks.stream()
            .mapToInt(Task::getStoryPoints)
            .sum();
    }
    
    @Transient
    public Integer getCompletedStoryPoints() {
        return tasks.stream()
            .filter(task -> "DONE".equals(task.getStatus()))
            .mapToInt(Task::getStoryPoints)
            .sum();
    }
} 
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
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Column(name = "project_id", nullable = false)
    private Integer projectId;
    
    @Column(name = "member_id")
    private Integer memberId;
    
    @ManyToOne
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    public void validateDates() {
        if (sprint != null) {
            if (startDate.isBefore(sprint.getStartDate()) || 
                dueDate.isAfter(sprint.getEndDate())) {
                throw new RuntimeException("Task dates must be within sprint date range");
            }
        }
    }
} 
package org.apache.dubbo.samples.seata.task.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.dubbo.samples.seata.api.entity.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "tasks")
@NoArgsConstructor
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

    @ManyToOne
    @JoinColumn(name = "member_id")
    private User member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    public void validateDates() {
        if (startDate == null || dueDate == null) {
            throw new RuntimeException("Start date and due date are required");
        }

        // if (startDate.isAfter(dueDate)) {
        //     throw new RuntimeException("Start date must be before due date");
        // }
        // if (sprint != null) {
        //     if (startDate.isBefore(sprint.getStartDate()) || 
        //         dueDate.isAfter(sprint.getEndDate())) {
        //         throw new RuntimeException("Task dates must be within sprint date range");
        //     }
        // }
    }
}

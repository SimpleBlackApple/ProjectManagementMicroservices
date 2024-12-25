package org.apache.dubbo.samples.seata.task.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "sprints")
@ToString(exclude = "tasks")
@EqualsAndHashCode(exclude = "tasks")
@NoArgsConstructor
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "project_id", nullable = false)
    private Integer projectId;
    
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    
    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> tasks = new HashSet<>();
} 
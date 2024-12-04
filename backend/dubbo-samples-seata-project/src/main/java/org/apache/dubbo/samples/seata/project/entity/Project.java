package org.apache.dubbo.samples.seata.project.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String name;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    
    @Column(name = "owner_id")
    private Integer ownerId;
}
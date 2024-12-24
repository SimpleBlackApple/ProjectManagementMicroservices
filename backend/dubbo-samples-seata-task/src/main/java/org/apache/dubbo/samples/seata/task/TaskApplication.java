package org.apache.dubbo.samples.seata.task;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EnableDubbo
@EntityScan(basePackages = {
    "org.apache.dubbo.samples.seata.api.entity",
    "org.apache.dubbo.samples.seata.task.entity"
})
public class TaskApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskApplication.class, args);
    }
} 
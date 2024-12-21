package org.apache.dubbo.samples.seata.user.config;

import org.apache.dubbo.samples.seata.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.dubbo.samples.seata.user.repository.UserRepository;

import java.time.LocalDateTime;

@Configuration
public class LoadUserDatabase {
    private static final Logger log = LoggerFactory.getLogger(LoadUserDatabase.class);

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() < 1) {
                log.info("Creating users");
                User user = new User("tester", "tester@example.com", "test");
                user.setCreatedAt(LocalDateTime.now());
                userRepository.save(user);
                log.info("Created test user.");
            }
        };
    }
}

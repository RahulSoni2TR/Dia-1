package com.example.webapp.cmd;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import com.example.webapp.models.Role;
import com.example.webapp.models.User;
import com.example.webapp.repository.RoleRepository;
import com.example.webapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //@Autowired
   // private PasswordEncoder passwordEncoder;

   // @Bean
   // public PasswordEncoder passwordEncoder()
   // {
   //     return new BCryptPasswordEncoder();
  //  }
    
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            Integer historyCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = DATABASE() AND table_name = 'rate_history'",
                Integer.class
            );
            boolean historyExists = historyCount != null && historyCount > 0;

            if (!historyExists) {
                jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS rate_history (" +
                    "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "rate_id BIGINT NULL," +
                    "commodity VARCHAR(50) NOT NULL," +
                    "old_price DECIMAL(10,2) NULL," +
                    "new_price DECIMAL(10,2) NOT NULL," +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ")"
                );
                logger.info("rate_history table created.");
            } else {
                logger.info("rate_history table already exists.");
            }

            // Create roles if they don't exist
            Optional<Role> adminRoles = roleRepository.findByRoleName("ROLE_ADMIN");
            Role adminRole = adminRoles.get();
            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setRoleName("ROLE_ADMIN");
                roleRepository.save(adminRole);
            }


            // Create a user if it doesn't exist
            if (userRepository.findByUsername("testuser") == null) {
                User user = new User();
                user.setUsername("testuser");
                user.setPassword("password");
                user.setQuestion("In what city were you born?");
                user.setAnswer("test");// Encode password
                user.getRoles().add(adminRole); // Assign the admin role
                userRepository.save(user);
            }
        };
    }
}
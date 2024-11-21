package com.example.webapp.cmd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import com.example.webapp.models.Role;
import com.example.webapp.models.User;
import com.example.webapp.repository.RoleRepository;
import com.example.webapp.repository.UserRepository;

//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

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
            // Create roles if they don't exist
            Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN");
            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setRoleName("ROLE_ADMIN");
                roleRepository.save(adminRole);
            }


            // Create a user if it doesn't exist
            if (userRepository.findByUsername("testuser") == null) {
                User user = new User();
                user.setUsername("testuser");
                user.setPassword("password"); // Encode password
                user.getRoles().add(adminRole); // Assign the admin role
                userRepository.save(user);
            }
        };
    }
}

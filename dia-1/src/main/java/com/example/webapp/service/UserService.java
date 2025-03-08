package com.example.webapp.service;

import com.example.webapp.models.Role;
import com.example.webapp.models.User;
import com.example.webapp.models.UserRoleUpdate;
import com.example.webapp.models.UserTemp;
import com.example.webapp.repository.RateRepository;
import com.example.webapp.repository.RoleRepository;
import com.example.webapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService{

    @Autowired
    private UserRepository userRepository;
    
	@Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<UserTemp> getAllUsers() {
    //	UserTemp userstemp= new UserTemp();
    	List<UserTemp> temp= new ArrayList<>();
        List<User> users= userRepository.findAll();
       for(User user:users) {
    	   UserTemp userstemp= new UserTemp();
    	//   System.out.println(user.getUsername());
    	   userstemp.setId(user.getId());
    	   userstemp.setUsername(user.getUsername());
    	   userstemp.setRoles(user.getRoles());
    	   temp.add(userstemp);
       }
       return temp;
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username) != null;
    }

    public void saveUser(User user) {
        // Here you should hash the password before saving
    //    user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }
    
    public void updatePermissions(List<UserRoleUpdate> updates) {
        // Map to store users by ID for efficient updates
        Map<Integer, User> userMap = new HashMap<>();

        // Fetch users in bulk to reduce database queries
        for (UserRoleUpdate update : updates) {
            if (!userMap.containsKey(update.getUserId())) {
                User user = userRepository.findById(update.getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + update.getUserId()));
                userMap.put(update.getUserId(), user);
            }
        }

        // Process each role update
        for (UserRoleUpdate update : updates) {
            User user = userMap.get(update.getUserId());
            Optional<Role> optionalRole = roleRepository.findByRoleName(update.getRole());
            if (!optionalRole.isPresent()) {
                throw new IllegalArgumentException("Role not found: " + update.getRole());
            }
            Role role = optionalRole.get();


            if (update.isAssigned()) {
                // Add role if it's not already assigned
                if (!user.getRoles().contains(role)) {
                    user.getRoles().add(role);
                }
            } else {
                // Remove role if it's currently assigned
                user.getRoles().remove(role);
            }
        }

        // Save updated users back to the database
        userRepository.saveAll(userMap.values());
    }
    
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }
 }

package com.example.webapp.controller;

import com.example.webapp.models.Role;
import com.example.webapp.models.User;
import com.example.webapp.service.CustomUserDetailsService;
import com.example.webapp.service.UserService;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private CustomUserDetailsService customservice;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/login")
    public String loginPage() {
        return "index"; // Render the login.html page
    }
    
    @PostMapping("/logout")
    public String logoutPage() {
        return "index"; // Render the logout.html page
    }

    @GetMapping("/home")
    public String home() {
        return "home";  // Renders the home.html page
    }
    
    @PostMapping("/process-login")
    public String processLogin(@RequestParam String username, @RequestParam String password) {
        // Retrieve user from the database
    	System.out.println("login endpoint");
        User user = userService.findByUsername(username);
       
        Iterator<Role> iterator = user.getRoles().iterator();
        while (iterator.hasNext()) {
            Role fruit = iterator.next();
            System.out.println(fruit.getRoleName());
        }
        
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
        	System.out.println("authentication complete");
            return "redirect:/home";  // Password matches, redirect to home
        } else {
        	System.out.println("authentication incomplete");
            return "index";  // Invalid login, return to login page
        }
    }
    

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password) {
        if (userService.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("Username already exists!");
        }

        // Here you may want to hash the password before saving
        User newUser = new User();
        newUser.setUsername(username);
        String encodedPassword = passwordEncoder.encode(password);
        newUser.setPassword(encodedPassword); // Replace with a hashed password

        // Optionally, you can set a default role for the user
        // Role defaultRole = roleService.findByRoleName("USER"); // Ensure to have a role service to fetch roles
        // newUser.setRoles(Collections.singletonList(defaultRole)); 

        userService.saveUser(newUser);
        
        return ResponseEntity.ok("User registered successfully!");
    }

}

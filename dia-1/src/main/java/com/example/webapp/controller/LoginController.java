package com.example.webapp.controller;

import com.example.webapp.models.ResetPasswordRequest;
import com.example.webapp.models.Role;
import com.example.webapp.models.User;
import com.example.webapp.models.UserTemp;
import com.example.webapp.service.CustomUserDetailsService;
import com.example.webapp.service.UserService;

import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

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
    public String processLogin(@RequestParam String username, @RequestParam String password,HttpSession session) {
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
        	session.setAttribute("username", user.getUsername());
        	System.out.println(user.getUsername());
            return "home";  // Password matches, redirect to home
        } else {
        	System.out.println("authentication incomplete");
            return "index";  // Invalid login, return to login page
        }
    }
    

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password, @RequestParam String securityQuestion,
    		@RequestParam String securityAnswer) {
        if (userService.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("Username already exists!");
        }

        // Here you may want to hash the password before saving
        User newUser = new User();
        newUser.setUsername(username);
        String encodedPassword = passwordEncoder.encode(password);
        newUser.setPassword(encodedPassword); // Replace with a hashed password
        newUser.setQuestion(securityQuestion);
        newUser.setAnswer(securityAnswer);
        // Optionally, you can set a default role for the user
        // Role defaultRole = roleService.findByRoleName("USER"); // Ensure to have a role service to fetch roles
        // newUser.setRoles(Collections.singletonList(defaultRole)); 

        userService.saveUser(newUser);
        
        return ResponseEntity.ok("User registered successfully!");
    }
    
    @GetMapping("/get-security-question")
    public ResponseEntity<Map<String, String>> getSecurityQuestion(@RequestParam String username) {
        Optional<User> user = Optional.ofNullable(userService.findByUsername(username));

        if (user.isPresent()) {
            return ResponseEntity.ok(Collections.singletonMap("question", user.get().getQuestion()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "User not found"));
    }

    @PostMapping("/verify-security-answer")
    public ResponseEntity<Map<String, Boolean>> verifySecurityAnswer(@RequestBody ResetPasswordRequest request) {
        Optional<User> userOptional = Optional.ofNullable(userService.findByUsername(request.getUsername()));

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getAnswer().equalsIgnoreCase(request.getSecurityAnswer())) {
                return ResponseEntity.ok(Collections.singletonMap("success", true));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("success", false));
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("success", false));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody ResetPasswordRequest request) {
        Optional<User> userOptional = Optional.ofNullable(userService.findByUsername(request.getUsername()));

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            user.setPassword(encodedPassword);
            userService.saveUser(user);
            return ResponseEntity.ok(Map.of(
            	    "message", "Password reset successful.",
            	    "success", true  // Ensure this field is present
            	));

        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found.","failure",false));
    }
    
    @PostMapping("/reset-ns-password")
    public ResponseEntity<Map<String,String>> resetNSPassword(@RequestBody ResetPasswordRequest request) {
    	System.out.println("here");
        Optional<User> userOptional = Optional.ofNullable(userService.findByUsername(request.getUsername()));
        System.out.println("Username from request: " + request.getUsername());
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println("Username from request: " + request.getUsername());

            // Validate old password
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            	System.out.println("aah inside");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "Old password is incorrect."));
            }

            // Ensure new password and confirm password match
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            	System.out.println("aah hhere");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", "New password and confirm password do not match."));
            }

            // Update password
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            user.setPassword(encodedPassword);
            userService.saveUser(user);
            System.out.println("came here");
            return ResponseEntity.ok(Collections.singletonMap("message", "Password reset successful."));
        }
        System.out.println("are we here");
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("error", "User not found."));
    }

}

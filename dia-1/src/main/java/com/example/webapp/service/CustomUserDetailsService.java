package com.example.webapp.service;

import com.example.webapp.models.User;
import com.example.webapp.models.Role;
import com.example.webapp.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private HttpSession session;
    
    public CustomUserDetailsService() {
        System.out.println("CustomUserDetailsService has been initialized.");
    }
    
    
    // Map roles to authorities, adding 'ROLE_' prefix
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
                    .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                    .collect(Collectors.toList());
    }


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("inside loaduserbyusername custom user");
		User user = userRepository.findByUsername(username);
        System.out.println("User is "+user.getUsername() +" and password is "+user.getPassword());
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        org.springframework.security.core.userdetails.User springUser = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                mapRolesToAuthorities(user.getRoles())
        );
        
        Set<Role> roless= user.getRoles();
        Set<String> newrole = new HashSet<>();
        for(Role role:roless) {
        	newrole.add(role.getRoleName());
        }
        session.setAttribute("username", user.getUsername());
        session.setAttribute("roles", newrole);
        System.out.println("Spring Security User Details: ");
        System.out.println("Username: " + springUser.getUsername());
        System.out.println("Password: " + springUser.getPassword());
        System.out.println("Authorities: " + springUser.getAuthorities());
        return springUser;

	}
}
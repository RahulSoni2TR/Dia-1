package com.example.webapp.models;

import java.util.HashSet;
import java.util.Set;

public class UserTemp {
	
	    private Integer id;

	    public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public Set<Role> getRoles() {
			return roles;
		}

		public void setRoles(Set<Role> roles) {
			this.roles = roles;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}


	    private String username;


	    private String password;


	    private Set<Role> roles = new HashSet<>();

	    // Getters and Setters
	}


package com.christianweaves.service;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class LoginService implements Serializable {

	private static final long serialVersionUID = 3094262587462847183L;

	private String username;
	private String password;
	
	public void authenticate() {
		System.out.println("Bing");
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
}

package com.sp.shop.dto;

public class UserResponseDTO {
	public Long getId() {
		return id;
	}
	public UserResponseDTO(Long id, String username, String email, String mobile, String role, boolean verified) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.mobile = mobile;
		this.role = role;
		this.verified = verified;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public boolean isVerified() {
		return verified;
	}
	public void setVerified(boolean verified) {
		this.verified = verified;
	}
		private Long id;
	    private String username;
	    private String email;
	    private String mobile;
	    private String role;
	    private boolean verified;
}

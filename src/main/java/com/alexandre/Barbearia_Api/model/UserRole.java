package com.alexandre.Barbearia_Api.model;

public enum UserRole {
    ADMIN("admin"),
    BARBEIRO("barbeiro"),
    USER("user");

    private String role;

    UserRole(String role){
        this.role = role;
    }

    public String getRole(){
        return role;
    }
}

package com.alexandre.Barbearia_Api.model;

public enum UserRole {
    ADMIN("admin"),
    DONO("dono"),
    FUNCIONARIO("funcionario"),
    USER("user");

    private final String role;

    UserRole(String role){
        this.role = role;
    }

    public String getRole(){
        return role;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isManager() {
        return this == DONO;
    }

    public boolean isReceptionist() {
        return this == FUNCIONARIO;
    }

    public boolean isBarber() {
        return this == FUNCIONARIO;
    }

    public boolean canAccessAdminPanel() {
        return isAdmin() || this == DONO || this == FUNCIONARIO;
    }

    public boolean canManageUsers() {
        return isAdmin() || this == DONO;
    }

    public boolean canManageUserRoles() {
        return isAdmin() || this == DONO;
    }

    public boolean canManageServices() {
        return isAdmin() || this == DONO;
    }

    public boolean canManageSchedule() {
        return isAdmin() || this == DONO || this == FUNCIONARIO;
    }

    public boolean canManageIndisponibilidade() {
        return isAdmin() || this == DONO || this == FUNCIONARIO;
    }

    public boolean canManageCommissions() {
        return isAdmin() || this == DONO || this == FUNCIONARIO;
    }

    public boolean canManageCash() {
        return isAdmin() || this == DONO;
    }

    public static UserRole from(String value) {
        if (value == null || value.isBlank()) return null;
        for (UserRole role : values()) {
            if (role.name().equalsIgnoreCase(value) || role.getRole().equalsIgnoreCase(value)) {
                return role;
            }
        }
        return null;
    }
}

package com.alexandre.Barbearia_Api.model;

public enum UserRole {
    ADMIN("admin"),
    GERENTE("gerente"),
    RECEPCIONISTA("recepcionista"),
    BARBEIRO("barbeiro"),
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
        return this == GERENTE;
    }

    public boolean isReceptionist() {
        return this == RECEPCIONISTA;
    }

    public boolean isBarber() {
        return this == BARBEIRO;
    }

    public boolean canAccessAdminPanel() {
        return isAdmin() || isManager() || isReceptionist();
    }

    public boolean canManageUsers() {
        return isAdmin() || isManager();
    }

    public boolean canManageUserRoles() {
        return isAdmin();
    }

    public boolean canManageServices() {
        return isAdmin() || isManager();
    }

    public boolean canManageSchedule() {
        return isAdmin() || isManager() || isReceptionist();
    }

    public boolean canManageIndisponibilidade() {
        return isAdmin() || isManager();
    }

    public boolean canManageCommissions() {
        return isAdmin() || isManager();
    }

    public boolean canManageCash() {
        return isAdmin() || isManager();
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

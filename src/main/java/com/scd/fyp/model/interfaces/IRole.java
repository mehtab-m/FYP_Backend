package com.scd.fyp.model.interfaces;

/**
 * Interface for Role entity
 * Defines contract for role-related operations
 */
public interface IRole {
    
    Long getRoleId();
    void setRoleId(Long roleId);
    
    String getRoleName();
    void setRoleName(String roleName);
}


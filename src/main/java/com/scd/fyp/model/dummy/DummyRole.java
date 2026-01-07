package com.scd.fyp.model.dummy;

import com.scd.fyp.model.interfaces.IRole;

/**
 * Dummy implementation of IRole
 * Used for testing and evaluation purposes
 */
public class DummyRole implements IRole {
    
    private Long roleId;
    private String roleName;
    
    public DummyRole() {
        // Default constructor
    }
    
    public DummyRole(Long roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }
    
    @Override
    public Long getRoleId() {
        return roleId;
    }
    
    @Override
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
    
    @Override
    public String getRoleName() {
        return roleName;
    }
    
    @Override
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    
    @Override
    public String toString() {
        return "DummyRole{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                '}';
    }
}


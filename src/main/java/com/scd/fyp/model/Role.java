package com.scd.fyp.model;

import com.scd.fyp.model.interfaces.IRole;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role implements IRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    private String roleName;

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}

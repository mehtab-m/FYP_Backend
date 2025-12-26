package com.scd.fyp.model;

import java.io.Serializable;

public class UserRoleId implements Serializable {
    private Long userId;
    private Long roleId;

    public UserRoleId() {}
    public UserRoleId(Long userId, Long roleId) { this.userId = userId; this.roleId = roleId; }
}

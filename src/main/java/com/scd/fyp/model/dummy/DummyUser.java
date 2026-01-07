package com.scd.fyp.model.dummy;

import com.scd.fyp.model.interfaces.IUser;

/**
 * Dummy implementation of IUser
 * Used for testing and evaluation purposes
 */
public class DummyUser implements IUser {
    
    private Long userId;
    private String name;
    private String email;
    private String password;
    private Integer semester;
    
    public DummyUser() {
        // Default constructor
    }
    
    public DummyUser(Long userId, String name, String email, String password, Integer semester) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.semester = semester;
    }
    
    @Override
    public Long getUserId() {
        return userId;
    }
    
    @Override
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String getEmail() {
        return email;
    }
    
    @Override
    public void setEmail(String email) {
        this.email = email;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public Integer getSemester() {
        return semester;
    }
    
    @Override
    public void setSemester(Integer semester) {
        this.semester = semester;
    }
    
    @Override
    public String toString() {
        return "DummyUser{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", semester=" + semester +
                '}';
    }
}


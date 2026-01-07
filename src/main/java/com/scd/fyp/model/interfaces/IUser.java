package com.scd.fyp.model.interfaces;


public interface IUser {
    
    Long getUserId();
    void setUserId(Long userId);
    
    String getName();
    void setName(String name);
    
    String getEmail();
    void setEmail(String email);
    
    String getPassword();
    void setPassword(String password);
    
    Integer getSemester();
    void setSemester(Integer semester);
}


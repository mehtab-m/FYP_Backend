package com.scd.fyp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "group_members")
public class GroupMember {

    @EmbeddedId
    private GroupMemberId id;

    @ManyToOne
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    private User student;

    // Getters & Setters
    public GroupMemberId getId() { return id; }
    public void setId(GroupMemberId id) { this.id = id; }

    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
}

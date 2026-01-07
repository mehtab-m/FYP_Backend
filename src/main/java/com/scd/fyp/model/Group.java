package com.scd.fyp.model;

import com.scd.fyp.model.interfaces.IGroup;
import jakarta.persistence.*;

@Entity
@Table(name = "groups")
public class Group implements IGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;

    private Long leaderId;

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Long getLeaderId() { return leaderId; }
    public void setLeaderId(Long leaderId) { this.leaderId = leaderId; }
}

package com.scd.fyp.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GroupMemberId implements Serializable {
    private Long groupId;
    private Long studentId;

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupMemberId)) return false;
        GroupMemberId that = (GroupMemberId) o;
        return Objects.equals(groupId, that.groupId) &&
                Objects.equals(studentId, that.studentId);
    }


    @Override
    public int hashCode() {
        return Objects.hash(groupId, studentId);
    }
}

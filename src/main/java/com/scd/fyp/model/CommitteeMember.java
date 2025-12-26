package com.scd.fyp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "committee_members")
public class CommitteeMember {

    @EmbeddedId
    private CommitteeMemberId id;

    @ManyToOne
    @MapsId("committeeId")
    @JoinColumn(name = "committee_id")
    private Committee committee;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    public CommitteeMemberId getId() { return id; }
    public void setId(CommitteeMemberId id) { this.id = id; }

    public Committee getCommittee() { return committee; }
    public void setCommittee(Committee committee) { this.committee = committee; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}

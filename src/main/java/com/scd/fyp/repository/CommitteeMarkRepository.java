package com.scd.fyp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.scd.fyp.model.CommitteeMark;

@Repository
public interface CommitteeMarkRepository extends JpaRepository<CommitteeMark, Long> {
}

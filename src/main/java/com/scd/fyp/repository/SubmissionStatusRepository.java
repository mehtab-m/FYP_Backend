package com.scd.fyp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.scd.fyp.model.SubmissionStatus;

@Repository
public interface SubmissionStatusRepository extends JpaRepository<SubmissionStatus, Long> {
}

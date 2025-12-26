package com.scd.fyp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.scd.fyp.model.Submission;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
}

package com.scd.fyp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.scd.fyp.model.Committee;

@Repository
public interface CommitteeRepository extends JpaRepository<Committee, Long> {
}

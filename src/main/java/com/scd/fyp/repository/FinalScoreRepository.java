package com.scd.fyp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.scd.fyp.model.FinalScore;

@Repository
public interface FinalScoreRepository extends JpaRepository<FinalScore, Long> {
}

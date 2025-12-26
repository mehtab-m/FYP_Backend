package com.scd.fyp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.scd.fyp.model.DocumentMarkScheme;

@Repository
public interface DocumentMarkSchemeRepository extends JpaRepository<DocumentMarkScheme, Long> {
}

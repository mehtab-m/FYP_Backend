// Repository: DocumentRepository
package com.scd.fyp.repository;

import com.scd.fyp.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
}

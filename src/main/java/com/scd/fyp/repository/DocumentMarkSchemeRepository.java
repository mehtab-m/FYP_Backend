// Repository: DocumentMarkSchemeRepository
package com.scd.fyp.repository;

import com.scd.fyp.model.DocumentMarkScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DocumentMarkSchemeRepository extends JpaRepository<DocumentMarkScheme, Long> {
    @Transactional
    void deleteByDocumentId(Long documentId);
}

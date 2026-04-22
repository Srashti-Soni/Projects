package com.vaultguard.aegis_engine.repository;

import com.vaultguard.aegis_engine.model.CodeScanEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CodeScanRepository extends JpaRepository<CodeScanEvent, Long> {
    List<CodeScanEvent> findByDeveloperId(String developerId);
    List<CodeScanEvent> findByResult(String result);
    List<CodeScanEvent> findByResultOrderByScannedAtDesc(String result);
}
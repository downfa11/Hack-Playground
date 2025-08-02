package com.ns.solve.repository.admin;

import com.ns.solve.domain.entity.admin.ProblemLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ProblemLogRepository extends JpaRepository<ProblemLog, Long> {
    List<ProblemLog> findByCreatedAtBetweenAndReportedFalse(LocalDateTime from, LocalDateTime to);
}

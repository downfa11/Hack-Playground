package com.ns.solve.service.admin;

import com.ns.solve.domain.entity.admin.ProblemLog;
import com.ns.solve.domain.vo.OperationType;
import com.ns.solve.repository.admin.ProblemLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemLogService {
    private final ProblemLogRepository logRepository;

    public void saveProblemLog(Long problemId, String problemTitle, String username, OperationType operationType) {
        ProblemLog log = new ProblemLog();
        log.setProblemId(problemId);
        log.setProblemTitle(problemTitle);
        log.setCreatorUsername(username);
        log.setOperationType(operationType);
        log.setCreatedAt(LocalDateTime.now());
        log.setReported(false);

        System.out.println("[INFO] saveProblemLog id:" + problemId+", title: "+problemTitle);
        logRepository.save(log);
    }

    public List<ProblemLog> getUnreportedLogs(LocalDateTime from, LocalDateTime to) {
        return logRepository.findByCreatedAtBetweenAndReportedFalse(from, to);
    }

    public void markAsReported(List<ProblemLog> logs) {
        logs.forEach(log -> log.setReported(true));
        logRepository.saveAll(logs);

        System.out.println("[INFO] send reported logs date:" +LocalDateTime.now());
    }
}


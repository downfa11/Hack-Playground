package com.ns.solve.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProblemReportScheduler {

    private final ProblemReportService reportService;

    @Scheduled(cron = "0 0 6 * * *") // 매일 오전 6시
    public void sendDailyProblemReport() {
        LocalDateTime now = LocalDateTime.now();

        try {
            log.info("ProblemReportScheduler Start - Current time: {}", now);
            reportService.sendReportAndMark();
        } catch (Exception e) {
            log.error("ProblemReportScheduler Error", e);
        }
    }
}

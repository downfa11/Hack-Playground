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

        LocalDateTime yesterday = now.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime today = now.withHour(0).withMinute(0).withSecond(0).withNano(0);

        try {
            log.info("ProblemReportScheduler Start - Current time: {}", now);
            reportService.sendReportAndMark(yesterday, today);
        } catch (Exception e) {
            log.error("ProblemReportScheduler Error", e);
        }
    }
}

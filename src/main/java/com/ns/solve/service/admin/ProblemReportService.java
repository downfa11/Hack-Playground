package com.ns.solve.service.admin;

import com.ns.solve.domain.dto.admin.GroupedProblemLog;
import com.ns.solve.domain.entity.admin.ProblemLog;
import com.ns.solve.repository.admin.EmailReceiverRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemReportService {

    private final ProblemLogService logService;
    private final EmailReceiverRepository emailReceiverRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Transactional
    public void sendReportAndMark() {
        List<ProblemLog> logs = logService.getUnreportedLogs();
        if (logs.isEmpty()) {
            log.info("보고서에 작성할 검수 문제가 없습니다.");
            return;
        }

        List<String> receivers = emailReceiverRepository.findAllEmails();
        if (receivers.isEmpty()) {
            log.info("보고서를 전송할 관리자 이메일 목록이 비어있습니다.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        String subject = "[Hplayground Report] " + from.toLocalDate();
        String content = buildHtmlReport(logs);

        receivers.forEach(toEmail -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(content, true);
                mailSender.send(message);
                logService.markAsReported(logs);

                log.info("[INFO] "+ toEmail +"에게 메일 발송이 완료했습니다.");
            } catch (Exception e) {
                log.warn("메일 전송 실패: {}", e.getMessage());
            }
        });
    }

    private String buildHtmlReport(List<ProblemLog> logs) {
        logs.sort(Comparator.comparing(ProblemLog::getCreatedAt));
        Map<String, GroupedProblemLog> groupedLogsMap = new LinkedHashMap<>();

        for (ProblemLog log : logs) {
            String key = log.getProblemId() + "::" + log.getProblemTitle() + "::" + log.getCreatorUsername();

            GroupedProblemLog groupedLog = groupedLogsMap.computeIfAbsent(key, k -> new GroupedProblemLog(log.getProblemTitle(), log.getCreatorUsername()));
            groupedLog.getOperationTypes().add(log.getOperationType());
            groupedLog.setLatestCreatedAt(log.getCreatedAt());
        }
        List<GroupedProblemLog> finalGroupedLogs = new ArrayList<>(groupedLogsMap.values());

        Context context = new Context();
        context.setVariable("logs", finalGroupedLogs);
        return templateEngine.process("problem-report", context);
    }
}

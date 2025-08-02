package com.ns.solve.service.admin;

import com.ns.solve.domain.entity.admin.ProblemLog;
import com.ns.solve.repository.admin.EmailReceiverRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemReportService {

    private final ProblemLogService logService;
    private final EmailReceiverRepository emailReceiverRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Transactional
    public void sendReportAndMark(LocalDateTime from, LocalDateTime to) {
        List<ProblemLog> logs = logService.getUnreportedLogs(from, to);
        if (logs.isEmpty()) return;

        List<String> receivers = emailReceiverRepository.findAllEmails();
        if (receivers.isEmpty()) return;

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
                log.info("[INFO] "+ toEmail +"에게 메일 발송이 완료했습니다.");
            } catch (Exception e) {
                log.warn("메일 전송 실패: {}", e.getMessage());
            }
        });

        logService.markAsReported(logs);
    }

    private String buildHtmlReport(List<ProblemLog> logs) {
        Context context = new Context();
        context.setVariable("logs", logs);
        return templateEngine.process("problem-report", context);
    }
}

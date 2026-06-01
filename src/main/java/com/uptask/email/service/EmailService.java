package com.uptask.email.service;

import com.uptask.config.EmailProperties;
import com.uptask.config.SecurityProperties;
import com.uptask.email.dto.EmailDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final EmailProperties emailProperties;
    private final SecurityProperties securityProperties;

    @Async("emailExecutor")
    public void sendActivationEmail(String to, String firstName, String otp) {
        var variables = new HashMap<String, Object>();
        variables.put("name", firstName);
        variables.put("otp", otp);
        variables.put("expirationMinutes", securityProperties.otpExpirationMinutes());
        send(new EmailDto(to, "Activate your account", "account-activation", variables));
    }

    @Async("emailExecutor")
    public void sendPasswordResetEmail(String to, String firstName, String otp) {
        var variables = new HashMap<String, Object>();
        variables.put("name", firstName);
        variables.put("otp", otp);
        variables.put("expirationMinutes", securityProperties.otpExpirationMinutes());
        send(new EmailDto(to, "Reset your password", "password-reset", variables));
    }

    @Async("emailExecutor")
    public void sendProjectInvitationEmail(String to, String inviterName, String projectName,
                                           String token, int expiryDays) {
        var variables = new HashMap<String, Object>();
        variables.put("inviterName", inviterName);
        variables.put("projectName", projectName);
        variables.put("token", token);
        variables.put("expiryDays", expiryDays);
        send(new EmailDto(to, "You've been invited to " + projectName, "project-invitation", variables));
    }

    private void send(EmailDto emailDto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailProperties.from(), emailProperties.fromName());
            helper.setTo(emailDto.to());
            helper.setSubject(emailDto.subject());

            Context context = new Context(Locale.getDefault());
            context.setVariables(emailDto.variables());
            String html = templateEngine.process(emailDto.templateName(), context);
            helper.setText(html, true);

            mailSender.send(message);
            log.debug("Email sent to {}: {}", emailDto.to(), emailDto.subject());
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", emailDto.to(), e.getMessage());
        }
    }
}

package nz.ac.canterbury.seng302.homehelper.service;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service for sending emails.
 *
 * @author Sean
 */
@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private JavaMailSender mailSender;
    private TemplateEngine htmlTemplateEngine;

    /**
     * EmailService constructor.
     *
     * @param mailSender {@link JavaMailSender} object from spring for sending
     * mail
     */
    @Autowired
    public EmailService(JavaMailSender mailSender, @Qualifier("emailTemplateEngine") TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.htmlTemplateEngine = templateEngine;
    }

    /**
     * Send a verification code to the specified email address.
     *
     * @param recipientEmail the recipient email address, assumed to be a valid
     * address
     * @param code the verification code string
     */
    @Async
    public void sendVerificationEmail(String recipientEmail,
            String recipientName, String code, 
            Locale locale) {
        final Context context = new Context(locale);
        context.setVariable("name", recipientName);
        context.setVariable("code", code);
        String subject = "Registration Confirmation";
        final String htmlContent = htmlTemplateEngine.process("html/email-register", context);
        final MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            message.setSubject(subject);
            message.setTo(recipientEmail);
            message.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException exception) {
            log.error("Verification email send failed: {}", exception);
        }

    }
}

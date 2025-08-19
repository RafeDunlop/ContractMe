package nz.ac.canterbury.seng302.homehelper.service;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final TemplateEngine htmlTemplateEngine;

    @Value("${app.domain}")
    private String domain;

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
        sendEmail(recipientEmail, subject, htmlContent, "Verification email send failed");
    }

    @Async
    public void sendPasswordResetEmail(String recipientEmail,
                                       String recipientName,
                                       String token,
                                       Locale locale) {
        final Context context = new Context(locale);
        context.setVariable("name", recipientName);
        context.setVariable("token", token);
        context.setVariable("domain", domain);
        String subject = "Password reset link";
        final String htmlContent = htmlTemplateEngine.process("html/email-forgot-password", context);
        sendEmail(recipientEmail, subject, htmlContent, "Password reset link send failed");
    }

    @Async
    public void createEmail(String recipientEmail, String subject, String pathToEmailHtml, Context context, String onFailureMessage) {
        final String htmlContent = htmlTemplateEngine.process(pathToEmailHtml, context);
        sendEmail(recipientEmail, subject, htmlContent, onFailureMessage);
    }

    private void sendEmail(String recipientEmail, String subject, String htmlContent, String errorMessage) {
        final MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            message.setSubject(subject);
            message.setTo(recipientEmail);
            message.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException exception) {
            logger.error(errorMessage, exception);
        }
    }

    /**
     * Send an update password confirmation message to the specified email address.
     *
     * @param recipientEmail the recipient email address, assumed to be a valid
     * address
     */
    @Async
    public void sendUpdatePasswordConfirmation(String recipientEmail,
                                      String recipientName,
                                      Locale locale) {
        final Context context = new Context(locale);
        context.setVariable("name", recipientName);
        String subject = "Password Updated";
        final String htmlContent = htmlTemplateEngine.process("html/email-update-password-confirmation", context);
        final MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            message.setSubject(subject);
            message.setTo(recipientEmail);
            message.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException exception) {
            logger.error("Update password confirmation email send failed", exception);
        }

    }

    /**
     * Sends an email to a contractor when they've been successfully added to a team
     *
     * @param recipientEmail the email of the recipient contractor
     */
    @Async
    public void sendRequestToContractor(String recipientEmail,
            String recipientName,
            String ownerName,
            String renovationName,
            String role,
            Locale locale,Long teamId) {
        final Context context = new Context(locale);
        context.setVariable("recipientEmail", recipientEmail);
        context.setVariable("name", recipientName);
        context.setVariable("owner", ownerName);
        context.setVariable("renovation", renovationName);
        context.setVariable("role", role);
        context.setVariable("domain", domain);
        context.setVariable("teamId", teamId);
        String subject = "Contractor Request";
        final String htmlContent = htmlTemplateEngine.process("html/email-confirm-team-request", context);
        sendEmail(recipientEmail, subject, htmlContent, "Contractor request send failed");
    }
}

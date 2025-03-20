package nz.ac.canterbury.seng302.homehelper.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Service for sending emails.
 *
 * @author Sean
 */
public class EmailService {

    private JavaMailSender mailSender;

    /**
     * EmailService constructor.
     *
     * @param mailSender {@link JavaMailSender} object from spring for sending
     * mail
     */
    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send a verification code to the specified email address.
     *
     * @param recipient the recipient email address, assumed to be a valid
     * address
     * @param code the verification code string
     */
    public void sendVerificationEmail(String recipient, String code) {
        String subject = "Registration Confirmation";
        String message = "Your email verification code is: " + code;
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setSubject(subject);
        mailMessage.setTo(recipient);
        mailMessage.setText(message);
        mailSender.send(mailMessage);
    }
}

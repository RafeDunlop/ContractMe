package nz.ac.canterbury.seng302.homehelper.event;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.security.GenerationStrategy;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Basic listener for the {@link OnRegistrationCompleteEvent} based on
 * <a href=https://www.baeldung.com/registration-verify-user-by-email>this tutorial.</a>
 *
 * @author Sean
 * @author Baeldung (tutorial)
 */
@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final JavaMailSender mailSender;

    private final VerificationCodeService verificationCodeService;

    @Autowired
    public RegistrationListener(JavaMailSender mailSender,
                                VerificationCodeService verificationCodeService) {
        this.mailSender = mailSender;
        this.verificationCodeService = verificationCodeService;
    }

    /**
     * Event callback function. Calls the private confirmRegistration function
     * which sends the email.
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    /**
     * Confirm registration by sending the confirmation email message.
     * @param event the event to respond to
     */
    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String code = verificationCodeService.issueSignupCode(
                GenerationStrategy.READABLE,
                user,
                event.getLocale()
        );
        String recipient = user.getEmail();
        String subject = "Registration Confirmation";
        String message = "Your email verification code is: " + code;
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(recipient);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        mailSender.send(mailMessage);
    }
}

package nz.ac.canterbury.seng302.homehelper.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.security.GenerationStrategy;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;

/**
 * Basic listener for the {@link OnRegistrationCompleteEvent} based on
 * <a href=https://www.baeldung.com/registration-verify-user-by-email>this
 * tutorial.</a>
 *
 * @author Sean
 * @author Baeldung (tutorial)
 */
@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final VerificationCodeService verificationCodeService;
    private final EmailService emailService;

    @Autowired
    public RegistrationListener(VerificationCodeService verificationCodeService,
            EmailService emailService) {
        this.verificationCodeService = verificationCodeService;
        this.emailService = emailService;
    }

    /**
     * Event callback function. Calls the private confirmRegistration function
     * which sends the email.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    /**
     * Confirm registration by sending the confirmation email message.
     *
     * @param event the event to respond to
     */
    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String code = verificationCodeService.issueVerificationCode(
                GenerationStrategy.READABLE,
                user,
                event.getLocale());
        String recipient = user.getEmail();
        String name = user.getFirstName();
        emailService.sendVerificationEmail(recipient, name, code, event.getLocale());
    }
}

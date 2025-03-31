package nz.ac.canterbury.seng302.homehelper.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.security.GenerationStrategy;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;

/**
 * Basic listener for the OnResetPasswordSubmittedEvent.
 */
@Component
public class ResetPasswordListener implements ApplicationListener<OnResetPasswordSubmittedEvent> {

    private final VerificationCodeService verificationCodeService;

    private final EmailService emailService;

    /**
     * Constructor for ResetPasswordListener
     * @param verificationCodeService Service to create and delete tokens
     * @param emailService Service to send emails to users
     */
    @Autowired
    public ResetPasswordListener(VerificationCodeService verificationCodeService,
                                EmailService emailService) {
        this.verificationCodeService = verificationCodeService;
        this.emailService = emailService;
    }

    /**
     * Event callback function. Calls the private confirmPasswordResetLink function
     * which sends the email.
     *
     * @param event The event to respond to
     */
    @Override
    public void onApplicationEvent(OnResetPasswordSubmittedEvent event) {
        this.confirmPasswordResetLink(event);
    }

    /**
     * Confirm password reset by sending the confirmation email message.
     *
     * @param event The event to respond to
     */
    private void confirmPasswordResetLink(OnResetPasswordSubmittedEvent event) {
        User user = event.getUser();
        String code = verificationCodeService.issueVerificationCode(
                GenerationStrategy.RESET_TOKEN,
                user,
                event.getLocale());
        String recipient = user.getEmail();
        String name = user.getFirstName();
        emailService.sendPasswordResetEmail(recipient, name, code, event.getLocale());
    }
}

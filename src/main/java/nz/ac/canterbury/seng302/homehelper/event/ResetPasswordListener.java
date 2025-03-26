package nz.ac.canterbury.seng302.homehelper.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.security.GenerationStrategy;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;

@Component
public class ResetPasswordListener implements ApplicationListener<OnResetPasswordSubmittedEvent> {

    private final VerificationCodeService verificationCodeService;
    private final EmailService emailService;

    @Autowired
    public ResetPasswordListener(VerificationCodeService verificationCodeService,
                                EmailService emailService) {
        this.verificationCodeService = verificationCodeService;
        this.emailService = emailService;
    }

    @Override
    public void onApplicationEvent(OnResetPasswordSubmittedEvent event) {
        this.confirmPasswordResetLink(event);
    }

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

package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.event.OnResetPasswordSubmittedEvent;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.thymeleaf.context.Context;
import java.util.*;

@Service
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final UserValidation userValidation;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ForgotPasswordService (UserRepository userRepository, UserValidation userValidation, ApplicationEventPublisher eventPublisher, EmailService emailService) {
        this.userRepository = userRepository;
        this.userValidation = userValidation;
        this.eventPublisher = eventPublisher;
        this.emailService = emailService;
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    public String validateEmail(String email, Locale locale) {
        Optional<User> expectedUser = userRepository.findByEmailIgnoreCase(email);

        expectedUser.ifPresent(user -> eventPublisher.publishEvent(new OnResetPasswordSubmittedEvent(user, locale)));
        return String.join("", userValidation.validateEmailString(email));
    }

    public void sendNewPasswordEmail(String email, String firstName, Locale locale) {
        Context context = new Context(locale);
        context.setVariable("name", firstName);
        emailService.createEmail(
                email,
                "",
                "html/email-confirm-new-password",
                context,
                "send confirmation of password change email failed"
        );
    }

    public List<String> validatePasswords(String newPassword, String confirmPassword) {
        return new ArrayList<>(userValidation.validatePasswordString(newPassword, confirmPassword, "resetPassword"));
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }
}

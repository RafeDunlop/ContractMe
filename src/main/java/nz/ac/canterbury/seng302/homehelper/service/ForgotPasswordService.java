package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.event.OnRegistrationCompleteEvent;
import nz.ac.canterbury.seng302.homehelper.event.OnResetPasswordSubmittedEvent;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final UserValidation userValidation;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public ForgotPasswordService (UserRepository userRepository, UserValidation userValidation, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.userValidation = userValidation;
        this.eventPublisher = eventPublisher;
    }

    public String validateEmail(String email, Locale locale) {
        Optional<User> expectedUser = userRepository.findByEmailIgnoreCase(email);
        expectedUser.ifPresent(user -> eventPublisher.publishEvent(new OnResetPasswordSubmittedEvent(user, locale)));
        return String.join("", userValidation.validateEmailString(email));
    }
}

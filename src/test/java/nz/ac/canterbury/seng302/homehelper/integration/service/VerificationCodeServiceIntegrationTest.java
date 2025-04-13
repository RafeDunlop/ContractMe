package nz.ac.canterbury.seng302.homehelper.integration.service;

import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import nz.ac.canterbury.seng302.homehelper.security.GenerationStrategy;
import nz.ac.canterbury.seng302.homehelper.service.RegisterService;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class VerificationCodeServiceIntegrationTest {

    @Autowired
    private RegisterService registerService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    private VerificationCodeService toTest;

    private static final long seed = 1234;

    private static final TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    private static final int timeQuantity = 100;

    private User user;

    private static final String email = "test@test.com";

    @BeforeEach
    void setUp() {
        toTest = new VerificationCodeService(verificationCodeRepository, userRepository);
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO(
                "test",
                "user",
                email,
                "P4$$word", "P4$$word"
        );
        user = registerService.registerUser(userRegisterDTO);
        toTest.setSeed(seed);
        toTest.setDelay(timeQuantity, timeUnit);
    }

    @AfterEach
    void clearRepository() {
        userRepository.deleteAll();
        verificationCodeRepository.deleteAll();
    }

    @Test
    public void issueSignupCodeAndConsume_allValid_userActivated() {
        String code = toTest.issueVerificationCode(GenerationStrategy.SIGNUP, user, Locale.ENGLISH);
        toTest.consumeSignupCode(code);
        assertTrue(userRepository.findByEmailIgnoreCase(email).get().isActivated());
    }

    @Test
    public void issueRestPasswordCodeAndConsume_allValid_codeDeleted() {
        String code = toTest.issueVerificationCode(GenerationStrategy.RESET_TOKEN, user, Locale.ENGLISH);
        toTest.consumeResetPasswordToken(code);
        assertTrue(verificationCodeRepository.findByCode(code).isEmpty());
    }

    @Test
    public void issueSignupCodeAndConsume_waitToExpire_throws() throws ExecutionException, InterruptedException {
        String code = toTest.issueVerificationCode(GenerationStrategy.SIGNUP, user, Locale.ENGLISH);
        toTest.getScheduledFutureDeletion(code).get();
        assertThrows(IllegalArgumentException.class, () -> toTest.consumeSignupCode(code));
    }

    @Test
    public void issueSignupCodeAndConsume_waitToExpire_userDeleted() throws ExecutionException, InterruptedException {
        String code = toTest.issueVerificationCode(GenerationStrategy.SIGNUP, user, Locale.ENGLISH);
        toTest.getScheduledFutureDeletion(code).get();
        assertFalse(userRepository.findByEmailIgnoreCase(email).isPresent());
    }

    @Test
    public void issueResetPasswordCodeAndConsume_waitToExpire_throws() throws ExecutionException, InterruptedException {
        String code = toTest.issueVerificationCode(GenerationStrategy.RESET_TOKEN, user, Locale.ENGLISH);
        toTest.getScheduledFutureDeletion(code).get();
        assertThrows(IllegalArgumentException.class, () -> toTest.consumeResetPasswordToken(code));
    }

    @Test
    public void issueResetPasswordCodeAndConsume_waitToExpire_codeDeleted() throws ExecutionException, InterruptedException {
        String code = toTest.issueVerificationCode(GenerationStrategy.RESET_TOKEN, user, Locale.ENGLISH);
        toTest.getScheduledFutureDeletion(code).get();
        assertFalse(verificationCodeRepository.findByCode(code).isPresent());
    }
}

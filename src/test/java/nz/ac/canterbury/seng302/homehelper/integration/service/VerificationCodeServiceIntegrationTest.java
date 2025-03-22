package nz.ac.canterbury.seng302.homehelper.integration.service;

import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.entity.VerificationCode;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import nz.ac.canterbury.seng302.homehelper.security.GenerationStrategy;
import nz.ac.canterbury.seng302.homehelper.service.RegisterService;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;

import nz.ac.canterbury.seng302.homehelper.validation.VerificationCodeValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class VerificationCodeServiceIntegrationTest {

    @Autowired
    private RegisterService registerService;

    @Autowired UserRepository userRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private VerificationCodeValidation verificationCodeValidation;

    private VerificationCodeService toTest;

    private static final long seed = 1234;

    private static final TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    private static final int timeQuantity = 100;

    private User user;

    private static final String email = "test@test.com";

    @BeforeEach
    void setUp() {
        toTest = new VerificationCodeService(verificationCodeRepository, verificationCodeValidation, userRepository);
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

    /**
     * todo: remove validation mock when implemented
     */
    @Test
    public void issueCodeAndConsume_allValid_userActivated() {
        String code = toTest.issueSignupCode(GenerationStrategy.READABLE, user, Locale.ENGLISH);
        when(verificationCodeValidation.isValid(any(VerificationCode.class), any(String.class), any(User.class))).thenReturn(true);
        toTest.consumeSignupCode(code);
        assertTrue(userRepository.findByEmailIgnoreCase(email).get().isActivated());
    }

    @Test
    public void issueCodeAndConsume_waitToExpire_throwsAndUserDeleted() throws ExecutionException, InterruptedException {
        String code = toTest.issueSignupCode(GenerationStrategy.READABLE, user, Locale.ENGLISH);
        toTest.getScheduledFutureDeletion(code).get();
        assertThrows(IllegalArgumentException.class, () -> toTest.consumeSignupCode(code));
        assertFalse(userRepository.findByEmailIgnoreCase(email).isPresent());
    }
}

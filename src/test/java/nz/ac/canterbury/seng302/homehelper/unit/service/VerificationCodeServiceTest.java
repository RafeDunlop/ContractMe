package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.entity.VerificationCode;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;
import nz.ac.canterbury.seng302.homehelper.validation.VerificationCodeValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class VerificationCodeServiceTest {

    private static final long seed = 1234;

    private static final String firstCode = "YbSxa3";

    private static final TimeUnit timeUnit = TimeUnit.SECONDS;

    private static final int timeQuantity = 5;

    private VerificationCodeService toTest;

    private VerificationCodeRepository verificationCodeRepository;

    private UserRepository userRepository;

    private VerificationCodeValidation verificationCodeValidation;

    private User user;

    private VerificationCode verificationCode;

    private AtomicReference<VerificationCode> generated;

    @BeforeEach
    public void setUp() {
        verificationCodeValidation = Mockito.mock(VerificationCodeValidation.class);
        verificationCodeRepository = Mockito.mock(VerificationCodeRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        user = Mockito.mock(User.class);
        verificationCode = Mockito.mock(VerificationCode.class);
        Mockito.when(verificationCode.getCode()).thenReturn(firstCode);
        Mockito.when(verificationCode.getUser()).thenReturn(user);
        Mockito.when(verificationCodeRepository.findByCode(firstCode)).thenReturn(Optional.of(verificationCode));
        Mockito.when(verificationCodeRepository.save(Mockito.any(VerificationCode.class)))
                .thenAnswer(invocation -> {
                    generated.set(invocation.getArgument(0));
                    return generated.get();
                });
        Mockito.when(verificationCodeRepository.findByCode(firstCode)).thenReturn(Optional.of(verificationCode));

        toTest = new VerificationCodeService(
                verificationCodeRepository,
                verificationCodeValidation,
                userRepository
        );
        toTest.setSeed(seed);
        toTest.setTiming(timeQuantity, timeUnit);
        generated = new AtomicReference<>();
    }

    @Test
    public void consumeSignupCode_validCode_doesntThrow() {
        Mockito.when(verificationCodeValidation.isValid(verificationCode, firstCode, user)).thenReturn(true);
        assertDoesNotThrow(() -> toTest.consumeSignupCode(firstCode));
    }

    @Test
    public void consumeSignupCode_invalidCode_throwsException() {
        Mockito.when(verificationCodeValidation.isValid(verificationCode, firstCode, user)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> toTest.consumeSignupCode(firstCode));
    }

    @Test
    public void consumeSignupCode_notInRepository_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> toTest.consumeSignupCode("notInRepository"));
    }

    @Test
    public void deleteSignupCodeAndAccount_codePresentAndExpired_deletesBoth() {
        AtomicReference<Boolean> userDeleted = new AtomicReference<>(false);
        AtomicReference<Boolean> verificationCodeDeleted = new AtomicReference<>(false);
        Mockito.doAnswer(invocation -> {
            userDeleted.set(true);
            return null;
        }).when(userRepository).delete(user);
        Mockito.doAnswer(invocation -> {
            verificationCodeDeleted.set(true);
            return null;
        }).when(verificationCodeRepository).delete(verificationCode);
        Mockito.when(verificationCodeRepository.findByCode(firstCode)).thenReturn(Optional.of(verificationCode));
        Mockito.when(verificationCode.isExpired()).thenReturn(true);
        toTest.deleteSignupCodeAndAccount(firstCode);
        assertTrue(userDeleted.get() && verificationCodeDeleted.get());
    }

    @Test
    public void issueVerificationCode_dontWait_stillExists() {
        toTest.issueVerificationCode(VerificationCodeService.GenerationStrategy.READABLE, user, Locale.ENGLISH);
        assertNotNull(generated.get());
    }

}

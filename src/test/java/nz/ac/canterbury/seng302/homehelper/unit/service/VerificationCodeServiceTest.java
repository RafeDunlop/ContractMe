package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.entity.VerificationCode;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import nz.ac.canterbury.seng302.homehelper.security.GenerationStrategy;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;
import nz.ac.canterbury.seng302.homehelper.validation.VerificationCodeValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class VerificationCodeServiceTest {

    private static final long seed = 1234;

    private static final String firstCode = "YbSxa3";

    private static final TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    private static final int timeQuantity = 100;

    private VerificationCodeService toTest;

    private VerificationCodeRepository verificationCodeRepository;

    private UserRepository userRepository;

    private VerificationCodeValidation verificationCodeValidation;

    private User user;

    private VerificationCode verificationCode;

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
        toTest = new VerificationCodeService(
                verificationCodeRepository,
                verificationCodeValidation,
                userRepository
        );
        toTest.setSeed(seed);
        toTest.setTiming(timeQuantity, timeUnit);
    }

    @Test
    public void consumeSignupCode_validCode_doesntThrowAndCodeDeleted() {
        Mockito.when(verificationCodeValidation.isValid(verificationCode, firstCode, user)).thenReturn(true);
        assertDoesNotThrow(() -> toTest.consumeSignupCode(firstCode));
        verify(verificationCodeRepository, times(1)).delete(verificationCode);
    }

    @Test
    public void consumeSignupCode_invalidCode_throwsExceptionAndCodeDeleted() {
        Mockito.when(verificationCodeValidation.isValid(verificationCode, firstCode, user)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> toTest.consumeSignupCode(firstCode));
        verify(verificationCodeRepository, times(1)).delete(verificationCode);
    }

    @Test
    public void consumeSignupCode_notInRepository_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> toTest.consumeSignupCode("notInRepository"));
    }

    @Test
    public void deleteSignupCodeAndAccount_codePresentAndExpired_deletesBoth() {
        Mockito.when(verificationCodeRepository.findByCode(firstCode)).thenReturn(Optional.of(verificationCode));
        Mockito.when(verificationCode.isExpired()).thenReturn(true);
        toTest.deleteSignupCodeAndAccount(firstCode);
        verify(verificationCodeRepository, times(1)).delete(verificationCode);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    public void issueVerificationCode_dontWait_stillExists() {
        toTest.issueVerificationCode(GenerationStrategy.READABLE, user, Locale.ENGLISH);
        verify(verificationCodeRepository, Mockito.never()).delete(verificationCode);
    }

    @Test
    public void issueVerificationCode_waitAndPresentAndExpired_hasBeenDeleted() throws InterruptedException, ExecutionException {
        String code = toTest.issueVerificationCode(GenerationStrategy.READABLE, user, Locale.ENGLISH);
        Mockito.when(verificationCodeRepository.findByCode(Mockito.any())).thenReturn(Optional.of(verificationCode));
        Mockito.when(verificationCode.isExpired()).thenReturn(true);
        toTest.getDeletionContract(code).get(); //waits for the scheduled service to finish
        verify(verificationCodeRepository, times(1)).delete(verificationCode);
    }
}

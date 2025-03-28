package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.entity.VerificationCode;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import nz.ac.canterbury.seng302.homehelper.security.GenerationStrategy;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;
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

    private static final String secondCode = firstCode + seed;

    private static final TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    private static final int timeQuantity = 100;

    private VerificationCodeService toTest;

    private VerificationCodeRepository verificationCodeRepository;

    private UserRepository userRepository;

    private User user;

    private VerificationCode verificationCodeOne;

    private VerificationCode verificationCodeTwo;

    @BeforeEach
    public void setUp() {
        verificationCodeRepository = Mockito.mock(VerificationCodeRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        user = Mockito.mock(User.class);
        verificationCodeOne = Mockito.mock(VerificationCode.class);
        verificationCodeTwo = Mockito.mock(VerificationCode.class);
        Mockito.when(verificationCodeOne.getCode()).thenReturn(firstCode);
        Mockito.when(verificationCodeOne.getUser()).thenReturn(user);
        Mockito.when(verificationCodeRepository.findByCode(firstCode)).thenReturn(Optional.of(verificationCodeOne));
        Mockito.when(verificationCodeTwo.getCode()).thenReturn(secondCode);
        Mockito.when(verificationCodeTwo.getUser()).thenReturn(user);
        Mockito.when(verificationCodeRepository.findByCode(secondCode)).thenReturn(Optional.of(verificationCodeTwo));
        toTest = new VerificationCodeService(
                verificationCodeRepository,
                userRepository
        );
        toTest.setSeed(seed);
        toTest.setDelay(timeQuantity, timeUnit);
    }

    @Test
    public void consumeSignupCode_validCode_doesntThrowAndCodeDeleted() {
        assertDoesNotThrow(() -> toTest.consumeSignupCode(firstCode));
        verify(verificationCodeRepository, times(1)).delete(verificationCodeOne);
    }

    @Test
    public void consumeSignupCode_invalidCode_throwsExceptionAndCodeDeleted() {
        Mockito.when(verificationCodeRepository.findByCode(secondCode)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> toTest.consumeSignupCode(secondCode));
        verify(verificationCodeRepository, Mockito.never()).delete(Mockito.any(VerificationCode.class));
    }

    @Test
    public void deleteSignupCodeAndAccount_codePresent_deletesBoth() {
        Mockito.when(verificationCodeRepository.findByCode(firstCode)).thenReturn(Optional.of(verificationCodeOne));
        toTest.deleteSignupCodeAndAccount(firstCode);
        verify(verificationCodeRepository, times(1)).delete(verificationCodeOne);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    public void issueSignupCode_dontWait_stillExists() {
        toTest.issueSignupCode(GenerationStrategy.READABLE, user, Locale.ENGLISH);
        verify(verificationCodeRepository, Mockito.never()).delete(verificationCodeOne);
    }

    @Test
    public void issueSignupCode_waitAndPresent_hasBeenDeleted() throws InterruptedException, ExecutionException {
        String code = toTest.issueSignupCode(GenerationStrategy.READABLE, user, Locale.ENGLISH);
        Mockito.when(verificationCodeRepository.findByCode(Mockito.any())).thenReturn(Optional.of(verificationCodeOne));
        toTest.getScheduledFutureDeletion(code).get(); //waits for the scheduled service to finish
        verify(verificationCodeRepository, times(1)).delete(verificationCodeOne);
    }

    @Test
    public void issueSignupCode_multipleAndWaitAndPresent_allHaveBeenDeleted() throws ExecutionException, InterruptedException {
        String codeOne = toTest.issueSignupCode(GenerationStrategy.READABLE, user, Locale.ENGLISH);
        String codeTwo = toTest.issueSignupCode(GenerationStrategy.READABLE, user, Locale.ENGLISH);
        Mockito.when(verificationCodeRepository.findByCode(codeOne)).thenReturn(Optional.of(verificationCodeOne));
        Mockito.when(verificationCodeRepository.findByCode(codeTwo)).thenReturn(Optional.of(verificationCodeTwo));
        toTest.getScheduledFutureDeletion(codeOne).get();
        toTest.getScheduledFutureDeletion(codeTwo).get();
        verify(verificationCodeRepository, times(2)).delete(Mockito.any(VerificationCode.class));
    }

    @Test
    public void issueSignupCode_waitAndPresentAndUserAlreadyActivated_throwsAndUserNotDeleted() {
        String code = toTest.issueSignupCode(GenerationStrategy.READABLE, user, Locale.ENGLISH);
        Mockito.when(verificationCodeRepository.findByCode(code)).thenReturn(Optional.of(verificationCodeOne));
        Mockito.when(user.isActivated()).thenReturn(true);
        assertThrows(ExecutionException.class, () -> toTest.getScheduledFutureDeletion(code).get());
        verify(userRepository, Mockito.never()).delete(Mockito.any(User.class));
    }
}

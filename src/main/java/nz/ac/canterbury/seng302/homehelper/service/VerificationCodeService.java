package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.verificationCode;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import nz.ac.canterbury.seng302.homehelper.validation.ValidationCodeValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class VerificationCodeService {

    private VerificationCodeRepository verificationCodeRepository;

    private ValidationCodeValidation validationCodeValidation;

    private LoginService loginService;

    private static final SecureRandom random = new SecureRandom();

    @Autowired
    public VerificationCodeService(VerificationCodeRepository verificationCodeRepository, ValidationCodeValidation validationCodeValidation, LoginService loginService) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.validationCodeValidation = validationCodeValidation;
        this.loginService = loginService;
    }


    public boolean consumeCode(Long signupCode) throws IllegalArgumentException {
        return false;
    }

    public void issueVerificationCode() {
        User currentUser = loginService.getUserByEmail();
        byte[] randomCodeBytes = new byte[32];
        random.nextBytes(randomCodeBytes);
        verificationCode verificationCode = new verificationCode(
                randomCodeBytes,
                currentUser,
                LocalDateTime.now().plusMinutes(10)
        );
        verificationCodeRepository.save(verificationCode);
    }

    public static void setRandomSeed(long seed) {
        random.setSeed(seed);
    }
}

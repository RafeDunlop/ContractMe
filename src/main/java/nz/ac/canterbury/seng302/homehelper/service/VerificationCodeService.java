package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.VerificationCode;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import nz.ac.canterbury.seng302.homehelper.validation.VerificationCodeValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VerificationCodeService {

    private static final long verificationCodeClearRateMS = 60000;

    private VerificationCodeRepository verificationCodeRepository;

    private VerificationCodeValidation verificationCodeValidation;

    private LoginService loginService;

    @Autowired
    public VerificationCodeService(VerificationCodeRepository verificationCodeRepository,
                                   VerificationCodeValidation verificationCodeValidation,
                                   LoginService loginService) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.verificationCodeValidation = verificationCodeValidation;
        this.loginService = loginService;
    }


    public boolean consumeCode(String verificationCode) throws IllegalArgumentException {
        Optional<VerificationCode> retrievedFromDb = verificationCodeRepository.findByCode(verificationCode);
        if (retrievedFromDb.isEmpty()) return false;
        if (verificationCodeValidation.isValid(retrievedFromDb.get(), verificationCode, loginService.getUserByEmail())) {
            verificationCodeRepository.delete(retrievedFromDb.get());
            return true;
        }
        return false;
    }

    public String issueVerificationCode() {
        User currentUser = loginService.getUserByEmail();
        String code = generateCode();
        VerificationCode verificationCode = new VerificationCode(
                currentUser,
                code
        );
        verificationCodeRepository.save(verificationCode);
        return code;
    }

    @Scheduled(fixedRate = verificationCodeClearRateMS)
    @Transactional
    public void removeExpiredCodes() {
    }

    private String generateCode() {
        // generate code
        // to consider: make sure the code is not already in use
        // ensure that the domain includes only characters that are easy to enter as a user
        return "";
    }
}

package nz.ac.canterbury.seng302.homehelper.validation;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.entity.VerificationCode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VerificationCodeValidation {

    private static boolean signupCodeIsExpired(LocalDateTime expiryTime) {
        return true;
    }

    public boolean isValid(VerificationCode verificationCode, String inputCode, User loggedInUser) {
        return false;
    }
}

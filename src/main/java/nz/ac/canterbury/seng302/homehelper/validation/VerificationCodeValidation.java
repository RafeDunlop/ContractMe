package nz.ac.canterbury.seng302.homehelper.validation;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.entity.VerificationCode;
import org.springframework.stereotype.Service;

@Service
public class VerificationCodeValidation {

    public boolean isValid(VerificationCode verificationCode, String inputCode, User loggedInUser) {
        return false;
    }
}

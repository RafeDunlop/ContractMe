package nz.ac.canterbury.seng302.homehelper.validation;

import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class SignupCodeValidation {

    private static boolean signupCodeIsExpired(Timestamp dateOfIssue) {
        return true;
    }
}

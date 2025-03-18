package nz.ac.canterbury.seng302.homehelper.validation;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ValidationCodeValidation {

    private static boolean signupCodeIsExpired(LocalDateTime expiryTime) {
        return true;
    }
}

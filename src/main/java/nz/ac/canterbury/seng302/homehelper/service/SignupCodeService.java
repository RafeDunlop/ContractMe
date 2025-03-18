package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.repository.SignupCodeRepository;
import nz.ac.canterbury.seng302.homehelper.validation.SignupCodeValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SignupCodeService {

    public SignupCodeRepository signupCodeRepository;

    public SignupCodeValidation signupCodeValidation;

    @Autowired
    public SignupCodeService(SignupCodeRepository signupCodeRepository, SignupCodeValidation signupCodeValidation) {
        this.signupCodeRepository = signupCodeRepository;
        this.signupCodeValidation = signupCodeValidation;
    }
}

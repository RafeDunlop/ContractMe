package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.dto.UpdatePasswordDTO;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UpdatePasswordService {
    private final UserValidation userValidation;

    @Autowired
    public UpdatePasswordService(UserValidation userValidation) {
        this.userValidation = userValidation;
    }


    public List<String> validatePassword(UpdatePasswordDTO updatePasswordDTO) {
        List<String> errors = new ArrayList<>();

        //Checks second two fields are the same and that the passwords match the patterns
        errors.addAll(userValidation.validatePasswordString(updatePasswordDTO.getNewPassword(), updatePasswordDTO.getRetypePassword(),"updatePassword"));
        return errors;
    }
}

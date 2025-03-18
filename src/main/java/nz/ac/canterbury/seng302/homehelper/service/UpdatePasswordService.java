package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.dto.UpdatePasswordDTO;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class UpdatePasswordService {
    private final UserValidation userValidation;
    private final LoginService loginService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UpdatePasswordService(UserValidation userValidation, LoginService loginService,UserRepository userRepository) {
        this.userValidation = userValidation;
        this.loginService = loginService;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        this.userRepository = userRepository;
    }


    public void updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        List<String> errors = new ArrayList<>();
        User user = loginService.getUserByEmail();
        String password = updatePasswordDTO.getNewPassword();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String email = user.getEmail();

        if (!passwordEncoder.matches(updatePasswordDTO.getCurrentPassword(), user.getPassword())){
            errors.add(String.format("Old Password does not match."));
        }
        errors.addAll(userValidation.validateUpdatePasswordString(password, updatePasswordDTO.getRetypePassword(),"registerPassword",firstName,lastName,email));
        //Checks second two fields are the same and that the passwords match the patterns
        errors.addAll(userValidation.validatePasswordString(updatePasswordDTO.getNewPassword(), updatePasswordDTO.getRetypePassword(),"updatePassword"));
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }
        // Updates the Users password to the new Password.
        user.setPassword(passwordEncoder.encode(password));

        //Save Users New Password
        userRepository.save(user);

    }


}

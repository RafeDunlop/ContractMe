package nz.ac.canterbury.seng302.homehelper.controller.support;

import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.ArrayList;

@Component
public class ControllerUserSupport {

    public void addUserDetails(Model model, User user) {

        if (!model.containsAttribute("firstName")) {
            model.addAttribute("firstName", user.getFirstName());
        }
        if (!model.containsAttribute("lastName")) {
            model.addAttribute("lastName", user.getLastName());
        }
        if (!model.containsAttribute("email")) {
            model.addAttribute("email", user.getEmail());
        }
        model.addAttribute("profilePicture", user.getProfilePicture());
    }

    public void addContractorDetails (Model model, Contractor contractor) {
        if (!model.containsAttribute("contractorDTO")) {
            UserRegisterDTO contractorDTO = new UserRegisterDTO();
            contractorDTO.setHourlyRate(contractor.getHourlyRate());
            contractorDTO.setPhoneNumber(contractor.getPhoneNumber());
            contractorDTO.setCountryCode(contractor.getCountryCode());
            contractorDTO.setSkills(new ArrayList<>(contractor.getSkills()));
            model.addAttribute("contractorDTO", contractorDTO);
            model.addAttribute("isAvailable", contractor.getAvailable());
        }
    }
}

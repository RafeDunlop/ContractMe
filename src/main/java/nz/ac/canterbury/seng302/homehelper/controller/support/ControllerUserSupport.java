package nz.ac.canterbury.seng302.homehelper.controller.support;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class ControllerUserSupport {

    public void addContractorDetails (Model model, Contractor contractor, HttpServletRequest request) {
        model.addAttribute("firstName", contractor.getFirstName());
        model.addAttribute("userType", "Contractor");
        model.addAttribute("phoneNumber", contractor.getPhoneNumberFormatted());
        model.addAttribute("hourlyRate", contractor.getHourlyRateFormatted(request.getLocale()));
        model.addAttribute("contractorSkills", contractor.getSkills());
        model.addAttribute("isAvailable", contractor.getAvailable());
        model.addAttribute("contractor", contractor);
    }
}

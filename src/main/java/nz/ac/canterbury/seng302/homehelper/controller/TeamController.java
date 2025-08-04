package nz.ac.canterbury.seng302.homehelper.controller;


import nz.ac.canterbury.seng302.homehelper.dto.CreateTeamDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * A controller for team management pages
 */
@Controller
@RequestMapping("/renovations/team")
public class TeamController {

    private final RenovationRecordService renovationRecordService;
    private final LoginService loginService;
    private final LocationService locationService;

    /**
     * Autowired constructor for instantiating a TeamController
     *
     * @param renovationRecordService the service associated with renovation records
     * @param loginService the login service for retrieving the logged-in user
     * @param locationService the location service to check if a record contains a valid location
     */
    @Autowired
    public TeamController(RenovationRecordService renovationRecordService, LoginService loginService, LocationService locationService) {
        this.renovationRecordService = renovationRecordService;
        this.loginService = loginService;
        this.locationService = locationService;
    }

    /**
     * Get mapping for the create team page
     * @param id the renovation record id
     * @param createTeamDTO the DTO containing the form fields
     * @param model object containing the model attributes for thymeleaf
     * @return a string referring to the HTML template for the create team page
     * @throws ResponseStatusException 404 not found if the record does not have a location, the current user does not
     *                                 own the renovation, or an argument is missing or invalid
     */
    @GetMapping("/create")
    public String createTeam(@RequestParam Long id, @ModelAttribute CreateTeamDTO createTeamDTO, Model model) {
        try {
            User loggedIn = loginService.getUserByEmail();
            RenovationRecord renovationRecord = renovationRecordService.getRecordById(id);
            if (renovationRecord == null || !loggedIn.equals(renovationRecord.getUser()) || !locationService.hasLocation(renovationRecord)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            //todo check that record doesn't already have a a team! This should be implemented by task "Implement form submission"
            model.addAttribute("createTeamDTO", createTeamDTO);
            model.addAttribute("renovationRecord", renovationRecord);
            model.addAttribute("skills", Skill.values());
            return "createTeam";
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/create")
    public String submitTeamRequest(@RequestParam Long id, @ModelAttribute CreateTeamDTO createTeamDTO, Model model) {
        // TODO this is a stub POST mapping to be implemented in task "Implement form submission"
        return "redirect:/renovations/team/create?id=" + id;
    }
}

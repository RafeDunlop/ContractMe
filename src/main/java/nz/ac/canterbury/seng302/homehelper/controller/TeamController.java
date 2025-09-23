package nz.ac.canterbury.seng302.homehelper.controller;


import jakarta.persistence.EntityNotFoundException;
import nz.ac.canterbury.seng302.homehelper.dto.TeamRequestDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.service.ContractorService;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.TeamsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * A controller for team management pages
 */
@Controller
@RequestMapping("/renovations/team")
public class TeamController {

    private static final Logger logger = LoggerFactory.getLogger(TeamController.class);


    private final RenovationRecordService renovationRecordService;
    private final LoginService loginService;
    private final LocationService locationService;
    private final TeamsService teamsService;
    private final ContractorService contractorService;


    /**
     * Autowired constructor for instantiating a TeamController
     *
     * @param renovationRecordService the service associated with renovation records
     * @param loginService            the login service for retrieving the logged-in user
     * @param locationService         the location service to check if a record contains a valid location
     * @param teamsService            the team service used for calling validation and creating roles, from the given request
     */
    @Autowired
    public TeamController(RenovationRecordService renovationRecordService, LoginService loginService, LocationService locationService, TeamsService teamsService,
                          ContractorService contractorService) {
        this.renovationRecordService = renovationRecordService;
        this.loginService = loginService;
        this.locationService = locationService;
        this.teamsService = teamsService;
        this.contractorService = contractorService;
    }

    /**
     * Get mapping for the create team page
     *
     * @param id             the renovation record id
     * @param teamRequestDTO the DTO containing the form fields
     * @param model          object containing the model attributes for thymeleaf
     * @return a string referring to the HTML template for the create team page
     * @throws ResponseStatusException 404 not found if the record does not have a location, the current user does not
     *                                 own the renovation, or an argument is missing or invalid
     */
    @GetMapping("/create")
    public String createTeam(@RequestParam Long id, @ModelAttribute TeamRequestDTO teamRequestDTO, Model model) {
        logger.info("GET /renovations/team/create");
        try {
            User loggedIn = loginService.getUserByEmail();
            RenovationRecord renovationRecord = renovationRecordService.getRecordById(id);
            if (renovationRecord == null || !loggedIn.equals(renovationRecord.getUser()) || !locationService.hasLocation(renovationRecord) || teamsService.teamExists(renovationRecord.getId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            model.addAttribute("teamRequestDTO", teamRequestDTO);
            model.addAttribute("renovationRecord", renovationRecord);
            model.addAttribute("skills", Skill.listOfSortedSkills());
            return "createTeam";
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Handles POST requests for creating a team for a renovation.
     *
     * @param teamRequestDTO The DTO representing the creation request.
     * @param id             Of the renovation record to create a team for.
     * @param model          The model used to pass data back to the view in case of validation errors.
     * @return A redirect to the renovation view page if the team is successfully created, or the create team page with errors displaying.
     */
    @PostMapping("/create")
    public String submitTeamRequest(TeamRequestDTO teamRequestDTO,
                                    @RequestParam(name = "id") Long id,
                                    Model model, RedirectAttributes redirectAttributes) {
        logger.info("POST /renovations/team/create");

        List<String> errors = teamsService.validateTeam(teamRequestDTO);
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("teamRequestDTO", teamRequestDTO);
            model.addAttribute("renovationRecord", renovationRecordService.getRecordById(id));
            model.addAttribute("skills", Skill.values());
            return "createTeam";
        }

        RenovationRecord teamRecord = renovationRecordService.getRecordById(id);
        String response = teamsService.createNewTeam(teamRecord, teamRequestDTO);

        redirectAttributes.addFlashAttribute("response", response.isEmpty());

        return "redirect:/renovations/view?id=" + id;
    }


    /**
     * Handler for a get request to the join team fragment.
     *
     * @return the join team fragment
     */
    @GetMapping("/join-team")
    public String joinTeam(Model model, @RequestParam("id") Long id) {
        User user = loginService.getUserByEmail();
        Team team = teamsService.getTeamById(id);
        RenovationRecord renovationRecord = team.getRenovationRecord();
        User owner = renovationRecord.getUser();
        String ownerName = owner.getFullName();
        Role role = teamsService.getContractorRole(user, team);
        model.addAttribute("skill", role.getSkill().getDisplayName());
        model.addAttribute("renovationName", renovationRecord.getName());
        model.addAttribute("ownerName", ownerName);
        model.addAttribute("profilePicture", owner.getProfilePicture());
        model.addAttribute("teamId", id);
        model.addAttribute("renovationId", renovationRecord.getId());
        return "fragments/joinTeam :: join-team";
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        try {
            User loggedIn = loginService.getUserByEmail();
            Team team = teamsService.getTeamById(id);
            RenovationRecord renovationRecord = team.getRenovationRecord();
            if (!renovationRecord.getUser().equals(loggedIn))
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            teamsService.deleteTeam(team);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Handler for a get request to the view team page.
     *
     * @return the view team page
     */
    @GetMapping("/view")
    public String viewTeam(Model model, @RequestParam("id") Long id) {
        try {
            User user = loginService.getUserByEmail();
            Team team = teamsService.getTeamById(id);

            Map<Long, Contractor> contractors = teamsService.getContractorsByTeamId(id);

            if (team.getRenovationRecord() != null
                    && team.getRenovationRecord().getUser() != null
                    && (
                    team.getRenovationRecord().getUser().getId().equals(user.getId())
                            || contractors.containsKey(user.getId())
            )
            ) {
                boolean isOwner = user == team.getRenovationRecord().getUser();
                model.addAttribute("team", team);
                model.addAttribute("contractors", contractors);
                model.addAttribute("isOwner", isOwner);
                return "viewTeam";
            }

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to view team");
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    /**
     * Removes the contractor from the role., redirects back to view team page
     *
     * @param teamId       of the team.
     * @param contractorId of the contractor
     * @return response based on whether the team id and contractor id  exists, if the user doesn't have permission to remove the contractor, or
     * if the deletion was successful
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteContractor(@RequestParam Long teamId, @RequestParam Long contractorId) {
        logger.info("DELETE /team/contractor/{}/{}", teamId, contractorId);
        Team team = teamsService.getTeamById(teamId);
        Contractor contractor = contractorService.getContractorById(contractorId);
        RenovationRecord renovationRecord = team.getRenovationRecord();
        if (renovationRecord == null) {
            return ResponseEntity.notFound().build();
        }
        if (!loginService.getUserByEmail().equals(renovationRecord.getUser())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        teamsService.deleteContractorFromTeam(team, contractor);
        return ResponseEntity.noContent().build();
    }
}

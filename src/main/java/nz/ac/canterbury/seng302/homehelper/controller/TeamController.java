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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/renovations/team")
public class TeamController {

    private final RenovationRecordService renovationRecordService;
    private final LoginService loginService;
    private final LocationService locationService;

    @Autowired
    public TeamController(RenovationRecordService renovationRecordService, LoginService loginService, LocationService locationService) {
        this.renovationRecordService = renovationRecordService;
        this.loginService = loginService;
        this.locationService = locationService;
    }

    @GetMapping("/create")
    public String createTeam(@RequestParam Long id, @ModelAttribute CreateTeamDTO createTeamDTO, Model model) {
        User loggedIn = loginService.getUserByEmail();
        RenovationRecord renovationRecord = renovationRecordService.getRecordById(id);
        if (loggedIn != renovationRecord.getUser() | ! locationService.hasLocation(renovationRecord)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        //todo check that record doesn't already have a a team!
        model.addAttribute("createTeamDTO", createTeamDTO);
        model.addAttribute("renovationRecord", renovationRecord);
        model.addAttribute("skills", Skill.values());
        return "createTeam";
    }
}

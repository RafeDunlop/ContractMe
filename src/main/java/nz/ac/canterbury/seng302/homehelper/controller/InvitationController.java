package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.service.ContractorService;
import nz.ac.canterbury.seng302.homehelper.service.TeamsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/invitations")
public class InvitationController {

    private static final Logger logger = LoggerFactory.getLogger(InvitationController.class);

    @Autowired
    TeamsService teamsService;

    @Autowired
    ContractorService contractorService;

    @GetMapping("/{teamId}/{userId}")
    public String viewInvitation(
            @PathVariable long teamId,
            @PathVariable long userId,
            Model model) {

        model.addAttribute("teamId", teamId);
        model.addAttribute("userId", userId);

        return "joinTeamInbox";
    }

    @PostMapping("/{teamId}/{userId}/accept")
    public String acceptInvitation(@PathVariable long teamId,
                                   @PathVariable long userId,
                                   RedirectAttributes redirectAttributes) {

        logger.info("POST /invitations/{}/{} accept", teamId, userId);

        Team team = teamsService.getTeamById(teamId);
        Contractor contractor = contractorService.getContractorById(userId);

        List<String> errors = new ArrayList<>();
        errors = teamsService.acceptContractor(contractor, team);

        return "redirect:/renovations/" + teamId;
    }

    @PostMapping("/{teamId}/{userId}/decline")
    public String declineInvitation(
            @PathVariable long teamId,
            @PathVariable long userId,
            RedirectAttributes redirectAttributes) {

        logger.info("POST /invitations/{}/{} decline", teamId, userId);

        Team team = teamsService.getTeamById(teamId);
        Contractor contractor = contractorService.getContractorById(userId);

        List<String> errors = new ArrayList<>();
        errors = teamsService.declineContractor(contractor, team);

        return "redirect:/renovations/" + teamId;
    }
}

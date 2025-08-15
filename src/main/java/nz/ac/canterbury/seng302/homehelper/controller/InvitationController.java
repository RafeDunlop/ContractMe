package nz.ac.canterbury.seng302.homehelper.controller;

import jakarta.persistence.EntityNotFoundException;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.service.ContractorService;
import nz.ac.canterbury.seng302.homehelper.service.TeamInvitationService;
import nz.ac.canterbury.seng302.homehelper.service.TeamsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/renovations/team/invitations")
public class InvitationController {

    private static final Logger logger = LoggerFactory.getLogger(InvitationController.class);

    TeamsService teamsService;

    TeamInvitationService teamInvitationService;

    ContractorService contractorService;

    @Autowired
    public InvitationController(TeamsService teamsService, ContractorService contractorService, TeamInvitationService teamInvitationService) {
        this.teamsService = teamsService;
        this.contractorService = contractorService;
        this.teamInvitationService = teamInvitationService;
    }

    @GetMapping("/{teamId}/{userId}")
    public String viewInvitation(
            @PathVariable long teamId,
            @PathVariable long userId,
            Model model) {

        model.addAttribute("teamId", teamId);
        model.addAttribute("userId", userId);
        try {
            Contractor contractor = contractorService.getContractorById(userId);
            Team team = teamsService.getTeamById(teamId);
            if (teamInvitationService.linkExpired(contractor, team)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Team invitation link is no longer valid.");
            }
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Team invitation link is no longer valid.");
        }

        return "joinTeamInbox";
    }

    @PostMapping("/{teamId}/{userId}/accept")
    public String acceptInvitation(@PathVariable long teamId,
                                   @PathVariable long userId,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {

        logger.info("POST /invitations/{}/{} accept", teamId, userId);

        Team team = teamsService.getTeamById(teamId);
        Contractor contractor = contractorService.getContractorById(userId);

        teamInvitationService.acceptContractor(contractor, team);

        return String.format("redirect:/renovations/view?id=%d", team.getRenovationRecord().getId());
    }

    @PostMapping("/{teamId}/{userId}/decline")
    public String declineInvitation(
            @PathVariable long teamId,
            @PathVariable long userId) {

        logger.info("POST /invitations/{}/{} decline", teamId, userId);

        Team team = teamsService.getTeamById(teamId);
        Contractor contractor = contractorService.getContractorById(userId);

        teamInvitationService.declineContractor(contractor, team);

        //todo redirect to inbox when it exists
        return "redirect:/main" + teamId;
    }
}

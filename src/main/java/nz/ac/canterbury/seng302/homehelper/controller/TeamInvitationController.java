package nz.ac.canterbury.seng302.homehelper.controller;

import jakarta.persistence.EntityNotFoundException;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.service.ContractorService;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
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

/**
 * Controller for handling contractor invitations to renovation teams
 * Provides endpoints for viewing, accepting, and declining invitations
 * All endpoints require the current user to be a contractor and logged in
 * If the invitation link is invalid or expired, a 404 (NOT_FOUND) is returned
 */
@Controller
@RequestMapping("/renovations/team/invitations")
public class TeamInvitationController {
    private static final Logger logger = LoggerFactory.getLogger(TeamInvitationController.class);
    private final LoginService loginService;

    TeamsService teamsService;
    TeamInvitationService teamInvitationService;
    ContractorService contractorService;

    /**
     * Creates a new controller for contractor invitations
     * @param teamsService service for accessing team data
     * @param contractorService service for accessing contractor data
     * @param teamInvitationService service for invitation logic
     * @param loginService service for getting the current logged-in user
     */
    @Autowired
    public TeamInvitationController(TeamsService teamsService, ContractorService contractorService, TeamInvitationService teamInvitationService, LoginService loginService) {
        this.teamsService = teamsService;
        this.contractorService = contractorService;
        this.teamInvitationService = teamInvitationService;
        this.loginService = loginService;
    }

    /**
     * Displays the invitation page for a given team.
     * @param teamId ID of the team the invitation belongs to
     * @param model model used to pass attributes to the view
     * @return the "joinTeamInbox" view if the invitation is valid
     * @throws ResponseStatusException 404 if the team does not exist or the link is invalid/expired
     */
    @GetMapping("/{teamId}")
    public String viewInvitation(
            @PathVariable long teamId,
            Model model) {

        model.addAttribute("teamId", teamId);
        Long userId = loginService.getUserByEmail().getId();
        try {
            Team team = teamsService.getTeamById(teamId);
            if (teamInvitationService.linkExpired(contractorService.getContractorById(userId), team)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Team invitation link is no longer valid.");
            }

            model.addAttribute("renovationId", team.getRenovationRecord().getId());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Team invitation link is no longer valid.");
        }

        return "joinTeamInbox";
    }

    /**
     * Accepts the contractors invitation to join the given team
     * @param teamId ID of the team to accept
     * @return redirect to the renovation view on success
     * @throws ResponseStatusException 404 if the team does not exist, or the invitation is invalid/expired,
     *                                 or the invitation was already accepted or declined
     */
    @PostMapping("/{teamId}/accept")
    public String acceptInvitation(@PathVariable long teamId) {
        logger.info("POST /invitations/{} accept", teamId);

        Long userId = loginService.getUserByEmail().getId();
        Team team = teamsService.getTeamById(teamId);
        Contractor contractor = contractorService.getContractorById(userId);

        try {
            if (teamInvitationService.linkExpired(contractorService.getContractorById(userId), team)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to accept invitation, link is no longer valid.");
            }
            teamInvitationService.acceptContractor(contractor, team);
        } catch (EntityNotFoundException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to accept invitation, link is no longer valid.");
        }

        return String.format("redirect:/renovations/view?id=%d", team.getRenovationRecord().getId());
    }

    /**
     * Declines the contractors invitation to join the given team
     * @param teamId ID of the team to decline
     * @return redirect to the main page on success todo change to inbox when done
     * @throws ResponseStatusException 404 if the team does not exist, or the invitation is invalid/expired,
     *                                 or the invitation was already accepted or declined
     */
    @PostMapping("/{teamId}/decline")
    public String declineInvitation(@PathVariable long teamId) {
        logger.info("POST /invitations/{} decline", teamId);

        Long userId = loginService.getUserByEmail().getId();
        Team team = teamsService.getTeamById(teamId);
        Contractor contractor = contractorService.getContractorById(userId);

        try {
            if (teamInvitationService.linkExpired(contractorService.getContractorById(userId), team)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to decline invitation, link is no longer valid.");
            }
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to decline invitation, link is no longer valid.");
        }

        teamInvitationService.declineContractor(contractor, team);

        //todo redirect to inbox when it exists
        return "redirect:/main";
    }
}

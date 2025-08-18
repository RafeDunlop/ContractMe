package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.TeamsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller for the requests inbox page.
 */
@Controller
public class RequestInboxController {
    private final LoginService loginService;
    private final TeamsService teamsService;

    private static final Logger logger = LoggerFactory.getLogger(RequestInboxController.class);
    /**
     * Autowired constructor for the request inbox controller
     * @param loginService login service for retrieving the current logged-in user
     * @param teamsService teams service for looking up team requests
     */
    @Autowired
    public RequestInboxController(LoginService loginService, TeamsService teamsService) {
        this.loginService = loginService;
        this.teamsService = teamsService;
    }

    /**
     * Get mapping for the view requests inbox.
     *
     * @param model the spring model to inject the team requests into
     * @return the template for the inbox if the user is a contractor, otherwise, redirect to /main
     */
    @GetMapping("/view-requests")
    public String requestInbox(Model model) {
        logger.info("GET /view-requests");
        User user = loginService.getUserByEmail();
        try {
            List<Team> teams = teamsService.getContractorTeamRequests(user);
            model.addAttribute("teams", teams);
            model.addAttribute("user", user);
        } catch (IllegalArgumentException e) {
            return "redirect:/main";
        }
        return "requestInboxTemplate";
    }
}

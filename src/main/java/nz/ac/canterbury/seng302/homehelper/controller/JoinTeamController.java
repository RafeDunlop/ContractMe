package nz.ac.canterbury.seng302.homehelper.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JoinTeamController {


    private static final Logger logger = LoggerFactory.getLogger(TeamController.class);


    public void JoinTeamController() {}

    /**
     * Handler for a get request to the join team fragment.
     * @return the join team fragment
     */
    @GetMapping("/join-team")
    public String joinTeam(Model model) {
        return "joinTeamInbox";
    }

}

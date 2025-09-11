package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.dto.MappedRenovation;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping("/map")
public class MapController {

    private final LoginService loginService;

    @Autowired
    public MapController(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * Gets renovations located within a box described by the co-ordinates specified
     * @param withPublic Whether to include public renovations not owned by the logged-in user within the range.
     *                   Default true
     * @param minLat The minimum latitude to be fetched
     * @param minLon The minimum longitude to be fetched
     * @param maxLat The maximum latitude to be fetched
     * @param maxLon The maximum longitude to be fetched
     * @return a {@link Collection} of {@link MappedRenovation} DTO objects which contain the minimal requisite details
     */
    @GetMapping("/renovations")
    public Collection<MappedRenovation> getByLocationInBounds(
            @RequestParam(required = false, defaultValue = "true") boolean withPublic,
            @RequestParam double minLat,
            @RequestParam double minLon,
            @RequestParam double maxLat,
            @RequestParam double maxLon) {
        User loggedIn = loginService .getUserByEmail();
        return List.of();
    }
}

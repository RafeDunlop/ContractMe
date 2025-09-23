package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.dto.CoordinateRectangle;
import nz.ac.canterbury.seng302.homehelper.dto.MappedContractor;
import nz.ac.canterbury.seng302.homehelper.dto.MappedRenovation;
import nz.ac.canterbury.seng302.homehelper.service.MapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;

@RestController
public class MapController {

    private final MapService mapService;
    private final TeamsService teamsService;
    private final LoginService loginService;

    private static final Logger logger = LoggerFactory.getLogger(MapController.class);

    @Autowired
    public MapController(MapService mapService, TeamsService teamsService, LoginService loginService) {
        this.mapService = mapService;
        this.teamsService = teamsService;
        this.loginService = loginService;
    }

    /**
     * Gets renovations located within a box described by the co-ordinates specified.
     * Specify bounding coords via bounds.[min/max][Lat/Lon]
     * @param withPublic Whether to include public renovations not owned by the logged-in user within the range.
     *                   Default true
     * @param coordinateRectangle Contains the longitude and latitudes values which determine the bounding rectangle
     * @return a {@link Collection} of {@link MappedRenovation} DTO objects which contain the minimal requisite details
     */
    @GetMapping("/map/renovations")
    public Collection<MappedRenovation> getByLocationInBounds(
            @RequestParam(required = false, defaultValue = "true") boolean withPublic,
            @ModelAttribute CoordinateRectangle coordinateRectangle) {
        logger.info("GET /map/renovations");
        List<MappedRenovation> mappedRenovationList = mapService.getRenovationsInBounds(coordinateRectangle, withPublic);
        logger.info("mapped renovations returned: {}", mappedRenovationList.size());
        return mappedRenovationList;
    }

    @GetMapping("/renovations/view/map/coords-rectangle")
    public List<Double> getMapBounds(@RequestParam(name = "id") Long id){
        return mapService.getCoordsFromRenovation(id);
    }

    /**
     * Returns a collection of contractors to be plotted on to the team map. The user calling this endpoint has to be
     * part of the team; otherwise an exception is returned.
     * @param id ID of the team
     * @return A collection of contractors to be plotted
     */
    @GetMapping("/contractors")
    public Collection<MappedContractor> getContractorByRenovationId(
            @RequestParam String id) {
        long teamId = Long.parseLong(id);
        User user = loginService.getUserByEmail();
        Team team = teamsService.getTeamById(teamId);
        RenovationRecord renovationRecord = team.getRenovationRecord();
        boolean isInTeam = teamsService.checkViewRenovationAccess(renovationRecord, user);
        if (isInTeam || renovationRecord.getUser() == user) {
            return teamsService.getMappedContractorsByTeamId(teamId);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}

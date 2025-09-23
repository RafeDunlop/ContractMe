package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.dto.CoordinateRectangle;
import nz.ac.canterbury.seng302.homehelper.dto.MappedContractor;
import nz.ac.canterbury.seng302.homehelper.dto.MappedRenovation;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.MapService;
import nz.ac.canterbury.seng302.homehelper.service.TeamsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

@RestController
@RequestMapping("/map")
public class MapController {

    private final MapService mapService;
    private final LoginService loginService;
    private final TeamsService teamsService;

    @Autowired
    public MapController(MapService mapService, LoginService loginService, TeamsService teamsService) {
        this.mapService = mapService;
        this.loginService = loginService;
        this.teamsService = teamsService;
    }

    /**
     * Gets renovations located within a box described by the co-ordinates specified.
     * Specify bounding coords via bounds.[min/max][Lat/Lon]
     * @param withPublic Whether to include public renovations not owned by the logged-in user within the range.
     *                   Default true
     * @param rawCoordinates Contains the raw longitude and latitudes values which determine the bounding rectangle
     * @return a {@link Collection} of {@link MappedRenovation} DTO objects which contain the minimal requisite details
     */
    @GetMapping("/renovations/{rawCoordinates}")
    public Collection<MappedRenovation> getByLocationInBounds(
            @RequestParam(required = false, defaultValue = "true") boolean withPublic,
            @PathVariable String rawCoordinates) {
        CoordinateRectangle coordinateRectangle = mapService.createCoordinateRectangle(rawCoordinates);
        return mapService.getRenovationsInBounds(coordinateRectangle, withPublic);
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
        if (isInTeam) {
            return teamsService.getMappedContractorsByTeamId(teamId);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}

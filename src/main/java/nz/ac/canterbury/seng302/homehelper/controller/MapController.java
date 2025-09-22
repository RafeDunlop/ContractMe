package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.dto.CoordinateRectangle;
import nz.ac.canterbury.seng302.homehelper.dto.MappedRenovation;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/map")
public class MapController {

    private final MapService mapService;
    private final TeamsService teamsService;
    private final LoginService loginService;

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

    @GetMapping("/contractors")
    public Collection<Contractor> getContractorByRenovationId(
            @RequestParam String id) {
        long teamId = Long.parseLong(id);
        User user = loginService.getUserByEmail();
        Team team = teamsService.getTeamById(teamId);
        RenovationRecord renovationRecord = team.getRenovationRecord();
        Boolean isInTeam = teamsService.checkViewRenovationAccess(renovationRecord, user);
//        if (isInTeam) {
            Map<Long, Contractor> contractorMap = teamsService.getContractorsByTeamId(teamId);
            return new ArrayList<>(contractorMap.values());
//        } else {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//        }
    }
}

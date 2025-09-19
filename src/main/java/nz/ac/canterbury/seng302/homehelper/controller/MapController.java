package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.dto.CoordinateRectangle;
import nz.ac.canterbury.seng302.homehelper.dto.MappedRenovation;
import nz.ac.canterbury.seng302.homehelper.service.MapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/map")
public class MapController {

    private final MapService mapService;

    private static final Logger logger = LoggerFactory.getLogger(MapController.class);

    @Autowired
    public MapController(MapService mapService) {
        this.mapService = mapService;
    }

    /**
     * Gets renovations located within a box described by the co-ordinates specified.
     * Specify bounding coords via bounds.[min/max][Lat/Lon]
     * @param withPublic Whether to include public renovations not owned by the logged-in user within the range.
     *                   Default true
     * @param coordinateRectangle Contains the longitude and latitudes values which determine the bounding rectangle
     * @return a {@link Collection} of {@link MappedRenovation} DTO objects which contain the minimal requisite details
     */
    @GetMapping("/renovations")
    public Collection<MappedRenovation> getByLocationInBounds(
            @RequestParam(required = false, defaultValue = "true") boolean withPublic,
            @ModelAttribute CoordinateRectangle coordinateRectangle) {
        logger.info("GET /map/renovations");
        List<MappedRenovation> mappedRenovationList = mapService.getRenovationsInBounds(coordinateRectangle, withPublic);
        logger.info("mapped renovations returned: {}", mappedRenovationList.size());
        return mappedRenovationList;
    }
}

package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.dto.CoordinateRectangle;
import nz.ac.canterbury.seng302.homehelper.dto.MappedRenovation;
import nz.ac.canterbury.seng302.homehelper.service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/map")
public class MapController {

    private final MapService mapService;

    private static final Map<String, String> MARKERS = Map.of(
            "user-renovation", "user-renovation.png",
            "public-renovation", "public-renovation.png",
            "contractor", "contractor.png"
    );

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
        return mapService.getRenovationsInBounds(coordinateRectangle, withPublic);
    }

    /**
     * Returns a PNG image for a map marker based on the type provided.
     * @param markerType The type of marker to retrieve
     * @return The PNG image of the marker
     */
    @GetMapping("/markers/{markerType}")
    public ResponseEntity<Resource> getMarker(@PathVariable String markerType) {
        if (MARKERS.containsKey(markerType)) {
            Resource resource = new ClassPathResource("/static/images/markers/" + MARKERS.get(markerType));
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);
        }
        return ResponseEntity.notFound().build();
    }
}

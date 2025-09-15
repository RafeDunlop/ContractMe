package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/map")
public class MapController {

    private static final Map<String, String> MARKERS = Map.of(
            "user-renovation", "user-renovation.png",
            "public-renovation", "public-renovation.png",
            "contractor", "contractor.png"
    );

    @GetMapping("/marker/{marker}")
    public ResponseEntity<Resource> getMarker(@PathVariable String marker) {
        if (MARKERS.containsKey(marker)) {
            Resource resource = new ClassPathResource("/static/images/markers/" + MARKERS.get(marker));
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            String imageType = "image/png";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(imageType))
                    .body(resource);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/renovations")
    public List<RenovationRecord> getRenovation() {
        List<RenovationRecord> renovations = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                RenovationRecord renovationRecord = new RenovationRecord();
                renovationRecord.setTags(new ArrayList<>());
                Location location = new Location();
                location.setLatitude(-43.52460 + i * 0.0001);
                location.setLongitude(172.57710 + j * 0.0001);
                renovationRecord.setLocation(location);
                renovations.add(renovationRecord);
            }
        }
        return renovations;
    }
}

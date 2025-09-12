package nz.ac.canterbury.seng302.homehelper.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

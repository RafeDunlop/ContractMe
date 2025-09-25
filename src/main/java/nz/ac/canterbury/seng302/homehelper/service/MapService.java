package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.dto.CoordinateRectangle;
import nz.ac.canterbury.seng302.homehelper.dto.MappedRenovation;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MapService {

    private final RenovationRecordRepository renovationRecordRepository;
    private final LoginService loginService;

    @Autowired
    public MapService(RenovationRecordRepository renovationRecordRepository, LoginService loginService) {
        this.renovationRecordRepository = renovationRecordRepository;
        this.loginService = loginService;
    }


    /**
     * Gets the lat and lon from a renovation and puts them in a map for subsequent json formatting
     * @param renovationId the id of the renovation being displayed
     * @return the map of latitude, longitude and their values
     */
    public Map<String, Double> getCoordsFromRenovation(Long renovationId) {
        Optional<RenovationRecord> optionalRecord = renovationRecordRepository.findById(renovationId);
        Map<String, Double> coords = new HashMap<>();

        if (optionalRecord.isPresent()) {
            RenovationRecord record = optionalRecord.get();
            Location renovationLocation = record.getLocation();
            double latitude = renovationLocation.getLatitude();
            coords.put("latitude", latitude);
            double longitude = renovationLocation.getLongitude();
            coords.put("longitude", longitude);
        }

        return coords;
    }

    /**
     * Gets the renovations within the specified range
     * @param coordinateRectangle DTO object which contains the bounding coordinates of the rectangle to get renovations for
     * @param withPublic Whether public renovations unowned by the logged-in user should be retrieved
     * @return {@link ArrayList} containing {@link MappedRenovation} which specify the minimal necessary data to display
     */
    public List<MappedRenovation> getRenovationsInBounds(CoordinateRectangle coordinateRectangle, boolean withPublic) {
        User user = loginService.getUserByEmail();
        List<MappedRenovation> owned = getOwned(user, coordinateRectangle).toList();
        ArrayList<MappedRenovation> mappings = new ArrayList<>(owned);
        if (withPublic) {
            Set<Long> ownedSet = owned.stream().map(MappedRenovation::getId).collect(Collectors.toSet());
            getPublic(coordinateRectangle).forEach(mappedRenovation -> {
                if (!ownedSet.contains(mappedRenovation.getId())) {
                    mappings.add(mappedRenovation);
                }
            });
        }
        return mappings;
    }

    private Stream<MappedRenovation> getOwned(User user, CoordinateRectangle coordinateRectangle) {
        Collection<MappedRenovation> owned = new ArrayList<>(renovationRecordRepository.findOwnedWithinBoxPositive(
                user,
                coordinateRectangle.getMinLat(),
                coordinateRectangle.getMinLon(),
                coordinateRectangle.getMaxLat()
        ).stream().map(renovation -> defaultMappingFunction(renovation, false)).toList());
        owned.addAll(renovationRecordRepository.findOwnedWithinBoxNegative(
                user,
                coordinateRectangle.getMinLat(),
                coordinateRectangle.getMaxLat(),
                coordinateRectangle.getMaxLon()
        ).stream().map(renovation -> defaultMappingFunction(renovation, false)).toList());

        return owned.stream();
    }

    private Stream<MappedRenovation> getPublic(CoordinateRectangle coordinateRectangle) {
        Collection<MappedRenovation> publicRecords = new ArrayList<>(renovationRecordRepository.findPublicWithinBoxPositive(
                        coordinateRectangle.getMinLat(),
                        coordinateRectangle.getMinLon(),
                        coordinateRectangle.getMaxLat()
        ).stream().map(renovation -> defaultMappingFunction(renovation, true)).toList());
        publicRecords.addAll(renovationRecordRepository.findPublicWithinBoxNegative(
                coordinateRectangle.getMinLat(),
                coordinateRectangle.getMaxLat(),
                coordinateRectangle.getMaxLon()
        ).stream().map(renovation -> defaultMappingFunction(renovation, true)).toList());

        return publicRecords.stream();
    }

    private MappedRenovation defaultMappingFunction(RenovationRecord renovation, boolean isPublic) {
        MappedRenovation mappedRenovation = new MappedRenovation();
        mappedRenovation.setId(renovation.getId());
        mappedRenovation.setLocation(renovation.getLocation());
        mappedRenovation.setName(renovation.getName());
        mappedRenovation.setUnownedPublic(isPublic);
        return mappedRenovation;
    }
}

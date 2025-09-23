package nz.ac.canterbury.seng302.homehelper.dto;

import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;

public class MappedContractor {
    private String fullName;
    private String email;
    private Location location;
    private String phoneNumberFormatted;
    private Skill skill;
    private Float hourlyRate;
}

package nz.ac.canterbury.seng302.homehelper.dto;

import nz.ac.canterbury.seng302.homehelper.entity.Location;

public class MappedRenovation {

    private long id;

    private String name;

    private Location location;

    public boolean isUnownedPublic() {
        return unownedPublic;
    }

    public void setUnownedPublic(boolean unownedPublic) {
        this.unownedPublic = unownedPublic;
    }

    private boolean unownedPublic;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}

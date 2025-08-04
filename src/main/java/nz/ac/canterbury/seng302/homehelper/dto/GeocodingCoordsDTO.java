package nz.ac.canterbury.seng302.homehelper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodingCoordsDTO {

    private double lat;

    private double lon;

    private Rank rank;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rank {
        private double confidence;

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getConfidence() {
        return rank.getConfidence();
    }

    public void setConfidence(double confidence) {
        rank.setConfidence(confidence);
    }

    public Rank getRank() { return rank; }

    public void setRank(Rank rank) { this.rank = rank; }
}

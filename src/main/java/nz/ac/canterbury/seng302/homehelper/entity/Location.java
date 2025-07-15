package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class Location {

    private String address;
    private String country;
    private String postcode;
    private String city;
    private String suburb;

    public Location(String address, String country, String postcode, String city, String suburb) {
        this.address = address;
        this.country = country;
        this.postcode = postcode;
        this.city = city;
        this.suburb = suburb;
    }

    public Location() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location that = (Location) o;
        return Objects.equals(address, that.address) &&
                Objects.equals(country, that.country) &&
                Objects.equals(postcode, that.postcode) &&
                Objects.equals(city, that.city) &&
                Objects.equals(suburb, that.suburb);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSuburb() {
        return suburb;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }
}

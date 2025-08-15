package nz.ac.canterbury.seng302.homehelper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import nz.ac.canterbury.seng302.homehelper.entity.Location;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressDTO {

    private AddressDataAttributionDTO datasource;

    private String name;

    private String country;

    private String postcode;

    private String city;

    private String street;

    private String region;

    private String housenumber;

    private String formatted;

    private double lat;

    private double lon;

    private String address_line1;

    private String address_line2;

    public AddressDataAttributionDTO getDatasource() {
        return datasource;
    }

    public void setDatasource(AddressDataAttributionDTO datasource) {
        this.datasource = datasource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHousenumber() {
        return housenumber;
    }

    public void setHousenumber(String housenumber) {
        this.housenumber = housenumber;
    }

    public String getFormatted() {
        return formatted;
    }

    public void setFormatted(String formatted) {
        this.formatted = formatted;
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

    public String getAddress_line1() {
        return address_line1;
    }

    public void setAddress_line1(String address_line1) {
        this.address_line1 = address_line1;
    }

    public String getAddress_line2() {
        return address_line2;
    }

    public void setAddress_line2(String address_line2) {
        this.address_line2 = address_line2;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AddressDTO addressDTO = (AddressDTO) o;
        return Objects.equals(country, addressDTO.country) && Objects.equals(postcode, addressDTO.postcode)
                && Objects.equals(city, addressDTO.city) && Objects.equals(region, addressDTO.region)
                && Objects.equals(lat, addressDTO.lat) && Objects.equals(lon, addressDTO.lon)
                && Objects.equals(address_line1, addressDTO.address_line1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, postcode, city, region, lat, lon, address_line1);
    }

    /**
     * Set the fields of this AddressDTO from the location provided.
     * @param location location object to set this DTO to match
     */
    public void setFromLocation(Location location) {
        setAddress_line1(location.getAddress());
        setCountry(location.getCountry());
        setPostcode(location.getPostcode());
        setCity(location.getCity());
        setRegion(location.getSuburb());
        setLat(location.getLatitude());
        setLon(location.getLongitude());
    }
}

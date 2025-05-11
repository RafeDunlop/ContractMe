package nz.ac.canterbury.seng302.homehelper.dto;

public class LocationDTO {
    public String address;
    public String suburb;
    public String city;
    public String postcode;
    public String country;

    public LocationDTO() {
        this.address = "";
        this.suburb = "";
        this.city = "";
        this.postcode = "";
        this.country = "";
    }


    public LocationDTO(String address, String suburb, String city, String postcode, String country) {
        this.address = address;
        this.suburb = suburb;
        this.city = city;
        this.postcode = postcode;
        this.country = country;
    }


    /**
     * @return address
     */
    public String getAddress() {return address;}

    /**
     * @return suburb
     */
    public String getSuburb() {return suburb;}

    /**
     * @return city
     */
    public String getCity() {return city;}

    /**
     * @return postcode
     */
    public String getPostcode() {return postcode;}

    /**
     * @return country
     */
    public String getCountry() {return country;}

    /**
     * @param address
     */
    public void setAddress(String address) {this.address = address;}

    /**
     * @param suburb
     */
    public void setSuburb(String suburb) {this.suburb = suburb;}

    /**
     * @param city
     */
    public void setCity(String city) {this.city = city;}

    /**
     * @param postcode
     */
    public void setPostcode(String postcode) {this.postcode = postcode;}

    /**
     * @param country
     */
    public void setCountry(String country) {this.country = country;}

}

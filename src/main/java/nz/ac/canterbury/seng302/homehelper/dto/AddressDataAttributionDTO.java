package nz.ac.canterbury.seng302.homehelper.dto;

public class AddressDataAttributionDTO {

    private String sourcename;

    private String attribution;

    private String license;

    private String url;

    public void setSourcename(String sourcename) {
        this.sourcename = sourcename;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSourcename() {
        return sourcename;
    }

    public String getAttribution() {
        return attribution;
    }

    public String getLicense() {
        return license;
    }

    public String getUrl() {
        return url;
    }
}

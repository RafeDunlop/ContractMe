package nz.ac.canterbury.seng302.homehelper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalisationDTO {

    private CountryDTO country;

    private CoordsDTO location;

    public List<LocalisationDataAttributionDTO> getDataSource() {
        return dataSource;
    }

    public void setDataSource(List<LocalisationDataAttributionDTO> dataSource) {
        this.dataSource = dataSource;
    }

    public CoordsDTO getLocation() {
        return location;
    }

    public void setLocation(CoordsDTO location) {
        this.location = location;
    }

    public CountryDTO getCountry() {
        return country;
    }

    public void setCountry(CountryDTO country) {
        this.country = country;
    }

    private List<LocalisationDataAttributionDTO> dataSource;

}

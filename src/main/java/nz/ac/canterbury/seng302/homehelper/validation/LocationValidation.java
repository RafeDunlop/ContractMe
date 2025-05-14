package nz.ac.canterbury.seng302.homehelper.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class LocationValidation {


    /**
     * Validates the specified city name based on the specified pattern in addition to the mandatory condition
     * that it must not be empty
     *
     * @param city The city name to be checked
     * @return true if the specified city name matches the specified pattern and is non-empty
     */
    public List<String> validateCity(String city) {
        List<String> errors = new ArrayList<>();
        Pattern pattern = Pattern.compile("^[\\p{L} \\-']+$", Pattern.UNICODE_CHARACTER_CLASS);
        String cityName = city.trim();
        if (!pattern.matcher(cityName).matches()) {
            errors.add("City contains invalid characters");
        }

        return errors;
    }

}

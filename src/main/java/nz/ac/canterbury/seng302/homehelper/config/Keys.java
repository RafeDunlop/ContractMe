package nz.ac.canterbury.seng302.homehelper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Holds api keys defined in application.properties as keys.[api-name] for easy extraction and injection
 * @author Rafe Dunlop
 */
@Component
@ConfigurationProperties(prefix = "keys")
public class Keys {

    private String geoapify;

    public String getGeoapify() {
        return geoapify;
    }

    public void setGeoapify(String geoapify) {
        this.geoapify = geoapify;
    }
}

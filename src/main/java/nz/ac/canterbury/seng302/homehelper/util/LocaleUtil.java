package nz.ac.canterbury.seng302.homehelper.util;

import java.util.Locale;

public class LocaleUtil {

    /**
     * Returns a locale with a region to allow for currencies to be selected based on a locale.
     * If the locale given doesn't contain a region, the returned locale will be set to English (New Zealand).
     * @param locale The locale of the request
     * @return A locale with a language and a region
     */
    public Locale getSafeLocale(Locale locale) {
        if (locale == null || locale.getCountry().isEmpty()) {
            return Locale.forLanguageTag("en-NZ");
        }
        return locale;
    }
}

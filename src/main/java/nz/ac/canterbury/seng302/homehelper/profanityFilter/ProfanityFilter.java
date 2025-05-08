package nz.ac.canterbury.seng302.homehelper.profanityFilter;

import nz.ac.canterbury.seng302.homehelper.profanityFilter.dictionary.Dictionary;
import nz.ac.canterbury.seng302.homehelper.profanityFilter.dictionary.Profanity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfanityFilter {

    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList(
            "ar", "az", "bg", "bs", "ca", "cs", "da", "de", "el", "en", "es", "et", "fi", "fr", "ga", "he", "hi", "hr",
            "hu", "hy", "id", "is", "it", "ja", "ka", "ko", "lt", "lv", "mk", "ms", "mt", "no", "nl", "pl", "pt", "ro",
            "ru", "sk", "sl", "sq", "sr", "sv", "sw", "th", "tl", "tr", "uk", "vi", "xh", "zh", "zu");

    private static Dictionary loadDictionary(String language) {
        String basePath = "src/main/resources/profanityFilterResources/com/modernmt/text/profanityFilterResources/";
        File file = new File(basePath + "dictionary." + language);

        try (InputStream stream = new FileInputStream(file)) {
            return Dictionary.read(language, stream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load dictionary file: " + file.getAbsolutePath(), e);
        }
    }
    private final Map<String, Dictionary.Matcher> matchers;

    public ProfanityFilter() {
        this(.3f);
    }

    public ProfanityFilter(float threshold) {
        matchers = new HashMap<>(SUPPORTED_LANGUAGES.size());
        for (String language : SUPPORTED_LANGUAGES) {
            Dictionary dictionary = loadDictionary(language);
            matchers.put(language, dictionary.matcher(threshold));
        }
    }

    public boolean test(String language, String text) {
        language = languageOf(language);

        Dictionary.Matcher matcher = matchers.get(language);
        return matcher != null && matcher.matches(text);
    }

    public Profanity find(String language, String text) {
        language = languageOf(language);

        Dictionary.Matcher matcher = matchers.get(language);
        return matcher != null ? matcher.find(text) : null;
    }

    private static String languageOf(String value) {
        int idx = value.indexOf('-');
        if (idx >= 0)
            value = value.substring(0, idx);

        if ("nn".equals(value) || "nb".equals(value))
            value = "no";

        return value;
    }

}

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

/**
 * ProfanityFilter class provides a singleton-based profanity detection system
 * that supports multiple languages. It uses language-specific dictionaries to test whether
 * a given text contains profanity and to locate profane words.
 * This implementation uses lazy loading for dictionaries, improving startup time by
 * loading each language dictionary only when first needed.
 */
public class ProfanityFilter {

    /**
     * The singleton instance of the {@code ProfanityFilter}.
     */
    private static final ProfanityFilter instance = new ProfanityFilter();

    /**
     * A list of ISO 639-1 language codes for which profanity filtering is supported.
     */
    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList(
            "ar", "az", "bg", "bs", "ca", "cs", "da", "de", "el", "en", "es", "et", "fi", "fr", "ga", "he", "hi", "hr",
            "hu", "hy", "id", "is", "it", "ja", "ka", "ko", "lt", "lv", "mk", "ms", "mt", "no", "nl", "pl", "pt", "ro",
            "ru", "sk", "sl", "sq", "sr", "sv", "sw", "th", "tl", "tr", "uk", "vi", "xh", "zh", "zu"
    );

    /**
     * Loads the dictionary for the specified language.
     *
     * @param language the ISO 639-1 language code
     * @return the loaded {@link Dictionary}
     * @throws RuntimeException if the dictionary file cannot be loaded
     */
    private static Dictionary loadDictionary(String language) {
        String basePath = "src/main/resources/profanityFilterResources/com/modernmt/text/profanityFilterResources/";
        File file = new File(basePath + "dictionary." + language);

        try (InputStream stream = new FileInputStream(file)) {
            return Dictionary.read(language, stream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load dictionary file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * A cache of profanity matchers per language.
     */
    private final Map<String, Dictionary.Matcher> matchers = new HashMap<>();

    /**
     * Private constructor for singleton pattern.
     */
    private ProfanityFilter() {}

    /**
     * Returns the singleton instance of {@code ProfanityFilter}.
     *
     * @return the singleton instance
     */
    public static ProfanityFilter getInstance() {
        return instance;
    }

    /**
     * Retrieves the matcher for the specified language, loading it if not already cached.
     *
     * @param language the language code
     * @return the {@link Dictionary.Matcher} for the language
     */
    private Dictionary.Matcher getMatcher(String language) {
        return matchers.computeIfAbsent(language, lang -> {
            Dictionary dictionary = loadDictionary(lang);
            return dictionary.matcher(0.0f);
        });
    }

    /**
     * Tests whether the specified text contains profanity in the given language.
     *
     * @param language the language code ("en" - english)
     * @param text     the text to test
     * @return {@code true} if the text contains profanity; {@code false} otherwise
     */
    public boolean test(String language, String text) {
        language = languageOf(language);
        Dictionary.Matcher matcher = getMatcher(language);
        return matcher != null && matcher.matches(text);
    }

    /**
     * Finds the first occurrence of profanity in the specified text for the given language.
     *
     * @param language the language code ("en" - english)
     * @param text     the text to search
     * @return a {@link Profanity} object representing the found profanity, or {@code null} if none is found
     */
    public Profanity find(String language, String text) {
        language = languageOf(language);
        Dictionary.Matcher matcher = getMatcher(language);
        return matcher != null ? matcher.find(text) : null;
    }

    /**
     * Normalizes the language code by converting regional variants to base language codes.
     * For example, "en-US" becomes "en", and Norwegian variants ("nn", "nb") become "no".
     *
     * @param value the original language code
     * @return the normalized language code
     */
    private static String languageOf(String value) {
        int idx = value.indexOf('-');
        if (idx >= 0)
            value = value.substring(0, idx);

        if ("nn".equals(value) || "nb".equals(value))
            value = "no";

        return value;
    }
}

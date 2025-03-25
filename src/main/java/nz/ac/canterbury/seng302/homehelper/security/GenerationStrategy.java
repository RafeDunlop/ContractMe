package nz.ac.canterbury.seng302.homehelper.security;

import java.security.SecureRandom;

/**
 * The strategy to be used for code generation.
 * Encapsulates the corresponding domain and code length
 */
public enum GenerationStrategy {

    /**
     * used for signup codes which may need to be manually copied from a phone etc., so must be shorter and contain
     * easy-to-copy characters. Uses Alphanumeric characters omitting one, zero and uppercase i and o,
     * as these can be ambiguous depending on font
     */
    READABLE(6, SecureRandomCodeGenerator.ALPHANUM.replaceAll("10IO", "")),

    /**
     * used for when the token doesn't need to be readable and does need to coem from a large domain i.e.
     * password reset
     */
    SECURE(32, SecureRandomCodeGenerator.BASE64URLDOMAIN);

    private final int codeLength;

    private final String domain;

    /**
     * Constructs the {@code SecureRandomCodeGenerator} used by this method
     * @param codeLength The length of the codes generated using this method
     * @param domain The domain to be used by the generator
     */
    GenerationStrategy(int codeLength, String domain) {
        this.codeLength = codeLength;
        this.domain = domain;
    }

    /**
     * Gets a valid {@link SecureRandomCodeGenerator} using this encoding method
     * @param seed The seed to use. If null (LIVE environment), the seed is random
     * @return A valid {@link SecureRandomCodeGenerator} using this encoding method
     */
    public SecureRandomCodeGenerator getGenerator(Long seed) {
        SecureRandom secureRandom = new SecureRandom();
        if (seed != null) secureRandom.setSeed(seed);
        return new SecureRandomCodeGenerator(
                secureRandom,
                codeLength,
                domain
        );
    }
}

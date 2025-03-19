package nz.ac.canterbury.seng302.homehelper.security;

import java.security.SecureRandom;

/**
 * Secure random code generator.
 * Based on code <a href=https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string>here</a>
 *
 * @author Sean Reitsma
 * @author stack overflow
 */
public class SecureRandomCodeGenerator {

    private final SecureRandom secureRandom;

    private final char[] symbols;
    private final char[] buf;
    
    /**
     * Initialise a secure random generator and set the length and symbols to use.
     *
     * @param length the lenght of characters each random code should be
     * @param symbols a string containing all the allowed characters for the code
     */
    public SecureRandomCodeGenerator(SecureRandom secureRandom, int length, String symbols) {
        if (length < 1) {
            throw new IllegalArgumentException();
        }
        if (symbols.length() < 2) {
            throw new IllegalArgumentException();
        }
        this.secureRandom = secureRandom;
        this.buf = new char[length];
        this.symbols = symbols.toCharArray();
    }

    /**
     * Generate a (secure) random string.
     *
     * @return a random string
     */
    public String nextString() {
        for (int idx = 0; idx < buf.length; ++idx) {
            buf[idx] = symbols[secureRandom.nextInt(symbols.length)];
        }
        return new String(buf);
    }
}

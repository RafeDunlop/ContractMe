package nz.ac.canterbury.seng302.homehelper.unit.security;

import java.security.SecureRandom;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import nz.ac.canterbury.seng302.homehelper.security.SecureRandomCodeGenerator;

public class SecureRandomCodeGeneratorTest {
    private SecureRandomCodeGenerator secureRandomCodeGenerator;
    private SecureRandom secureRandom;

    @BeforeEach
    public void setUp() {
        secureRandom = Mockito.mock(SecureRandom.class);
    }

    @Test
    public void generateCode_validLength_returnsCorrectResult() {
        secureRandomCodeGenerator = new SecureRandomCodeGenerator(
                secureRandom, 6, SecureRandomCodeGenerator.ALPHANUM);
        Mockito.when(secureRandom.nextInt(Mockito.anyInt())).thenReturn(1);
        String expectedResult = "BBBBBB";
        String actualResult = secureRandomCodeGenerator.nextString();
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void initialiseGenerator_invalidLength_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            secureRandomCodeGenerator = new SecureRandomCodeGenerator(
                    secureRandom, 0, SecureRandomCodeGenerator.ALPHANUM);
        });
    }

    @Test
    public void initialiseGenerator_invalidSymbols_throwsException() {
        String invalidSymbols = "a";
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            secureRandomCodeGenerator = new SecureRandomCodeGenerator(secureRandom, 6, invalidSymbols);
        });
    }

    @Test
    public void initialiseGenerator_validSymbolsBoundary_doesNotThrow() {
        String validSymbols = "aa";
        Assertions.assertDoesNotThrow(() -> {
            secureRandomCodeGenerator = new SecureRandomCodeGenerator(
                    secureRandom, 6, validSymbols);
        });
    }
}

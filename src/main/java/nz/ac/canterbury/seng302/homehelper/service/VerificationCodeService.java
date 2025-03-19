package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.VerificationCode;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import nz.ac.canterbury.seng302.homehelper.security.SecureRandomCodeGenerator;
import nz.ac.canterbury.seng302.homehelper.validation.VerificationCodeValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class VerificationCodeService {

    private static final long verificationCodeBaseClearRateInMS = 60000;

    private final VerificationCodeRepository verificationCodeRepository;

    private final VerificationCodeValidation verificationCodeValidation;

    private final LoginService loginService;

    private Long randomSeed;

    @Autowired
    public VerificationCodeService(VerificationCodeRepository verificationCodeRepository,
                                   VerificationCodeValidation verificationCodeValidation,
                                   LoginService loginService) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.verificationCodeValidation = verificationCodeValidation;
        this.loginService = loginService;
    }


    public boolean consumeCode(String verificationCode) throws IllegalArgumentException {
        Optional<VerificationCode> retrievedFromDb = verificationCodeRepository.findByCode(verificationCode);
        if (retrievedFromDb.isEmpty()) return false;
        if (verificationCodeValidation.isValid(retrievedFromDb.get(), verificationCode, loginService.getUserByEmail())) {
            verificationCodeRepository.delete(retrievedFromDb.get());
            return true;
        }
        return false;
    }

    public String issueVerificationCode(GenerationStrategy generationStrategy, User user, Locale locale) {
        SecureRandomCodeGenerator secureRandomCodeGenerator = generationStrategy.getGenerator(randomSeed);
        String code = generateUniqueCode(secureRandomCodeGenerator);
        VerificationCode verificationCode = new VerificationCode(
                user,
                code,
                locale
        );
        verificationCodeRepository.save(verificationCode);
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.schedule(this::removeExpiredCodes, 10, TimeUnit.MINUTES);
        return code;
    }

    private String generateUniqueCode(SecureRandomCodeGenerator secureRandomCodeGenerator) {
        String code;
        boolean unique;
        do  {
            code = secureRandomCodeGenerator.nextString();
            unique = verificationCodeRepository.findByCode(code).isEmpty();
        } while (!unique);
        return code;
    }

    @Scheduled(fixedRate = verificationCodeBaseClearRateInMS)
    @Transactional
    public void removeExpiredCodes() {
    }

    public void setSeed(long seed) {
        randomSeed = seed;
    }

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
}

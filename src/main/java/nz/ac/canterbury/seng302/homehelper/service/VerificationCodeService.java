package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.VerificationCode;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import nz.ac.canterbury.seng302.homehelper.security.GenerationStrategy;
import nz.ac.canterbury.seng302.homehelper.security.SecureRandomCodeGenerator;
import nz.ac.canterbury.seng302.homehelper.validation.VerificationCodeValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final long verificationCodeBaseClearRateInMS = 3600000; // one hour in ms

    private static final Logger logger = LoggerFactory.getLogger(VerificationCodeService.class);

    private final VerificationCodeRepository verificationCodeRepository;

    private final VerificationCodeValidation verificationCodeValidation;

    private final UserRepository userRepository;

    private Long randomSeed;

    private TimeUnit timeUnit = TimeUnit.MINUTES;

    private int timeQuantity = 10;

    @Autowired
    public VerificationCodeService(VerificationCodeRepository verificationCodeRepository,
                                   VerificationCodeValidation verificationCodeValidation,
                                   UserRepository userRepository) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.verificationCodeValidation = verificationCodeValidation;
        this.userRepository = userRepository;
    }


    /**
     * Verify the sign up code, consume it, and set the user active.
     *
     * @param signupCode the code the user was emailed
     * @throws IllegalArgumentException if the code is invalid
     */
    public void consumeSignupCode(String signupCode) throws IllegalArgumentException {
        Optional<VerificationCode> verificationCodeOptional = verificationCodeRepository.findByCode(signupCode);
        if (verificationCodeOptional.isPresent()) {
            VerificationCode verificationCode = verificationCodeOptional.get();
            User user = verificationCode.getUser();
            if (verificationCodeValidation.isValid(verificationCode, signupCode, user)) {
                verificationCode.getUser().activate();
                verificationCodeRepository.delete(verificationCode);
                return;
            }
        }
        throw new IllegalArgumentException("Signup code invalid");
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
        scheduledExecutorService.schedule(() -> deleteSignupCodeAndAccount(code), timeQuantity, timeUnit);
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

    /**
     * removes ALL expired codes
     * /todo deprecated
     */
    @Scheduled(fixedRate = verificationCodeBaseClearRateInMS)
    @Transactional
    public void removeExpiredCodes() {

    }

    public void deleteSignupCodeAndAccount(String code) {
        Optional<VerificationCode> verificationCodeOptional = verificationCodeRepository.findByCode(code);
        if (verificationCodeOptional.isPresent()) {
            VerificationCode verificationCode = verificationCodeOptional.get();
            if (verificationCode.isExpired()) {
                User user = verificationCode.getUser();
                if (user.isActivated()) {
                    throw new IllegalStateException("An expired signup code exists whose associated user is activated");
                }
                logger.info("deleting user whose signup code expired. User: {}, code: {}", user, code);
                userRepository.delete(user);
                verificationCodeRepository.delete(verificationCode);
            } else {
                logger.warn("The verification code {} claims not to have expired. {}",
                        code,
                        "This should only happen if the same code is re-issued after being consumed within 10 minutes"
                );
            }
        }
    }

    public void setSeed(long seed) {
        randomSeed = seed;
    }

    public void setTiming(int timeQuantity, TimeUnit timeUnit) {
        this.timeQuantity = timeQuantity;
        this.timeUnit = timeUnit;
    }

}

package nz.ac.canterbury.seng302.homehelper.service;

import jakarta.annotation.PreDestroy;
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
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Access point for all domain functionality for {@link VerificationCode} including the issuing, consuming and automatic
 * expiration of a {@link VerificationCode}
 * @author Rafe Dunlop
 */
@Service
public class VerificationCodeService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationCodeService.class);

    private final VerificationCodeRepository verificationCodeRepository;

    private final VerificationCodeValidation verificationCodeValidation;

    private final UserRepository userRepository;

    private Long randomSeed;

    private TimeUnit timeUnit = TimeUnit.MINUTES;

    private int timeQuantity = 10;

    private final HashMap<String, ScheduledFuture<?>> deletionContractMap;

    private final ScheduledExecutorService deletionExecutor;

    /**
     * Constructs repositories and validation classes as required.
     * Makes a single deletion executor thread to delete expired codes with a daemon thread
     * @param verificationCodeRepository The repository for {@link VerificationCode} entities
     * @param verificationCodeValidation The validation class for {@link VerificationCode} entities
     * @param userRepository The repository for {@link User} entities
     */
    @Autowired
    public VerificationCodeService(VerificationCodeRepository verificationCodeRepository,
                                   VerificationCodeValidation verificationCodeValidation,
                                   UserRepository userRepository) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.verificationCodeValidation = verificationCodeValidation;
        this.userRepository = userRepository;
        deletionContractMap = new HashMap<>();
        deletionExecutor = Executors.newSingleThreadScheduledExecutor(threadTask -> {
            Thread thread = new Thread(threadTask);
            thread.setDaemon(true); //ensures that threads do not block JVM shutdown
            return thread;
        });
    }

    /**
     * Ensures both that all tasks are executed and that the executor thread is returned to the thread pool before this
     * service is destroyed by spring magic
     */
    @PreDestroy
    public void shutdown()  {
        deletionExecutor.shutdown();
    }

    /**
     * Gets the Scheduled future object which promises to execute {@link #deleteSignupCodeAndAccount} after the specified time
     * @param code The {@code String} code whose {@code ScheduledFuture} deletion is retrieved
     * @return The {@code ScheduledFuture} of the specified code
     */
    public ScheduledFuture<?> getScheduledFutureDeletion(String code) {
        return deletionContractMap.get(code);
    }

    /**
     * sets the seed to be used for randomness in the case where consistency is required
     * @param seed The seed to be set
     */
    public void setSeed(long seed) {
        randomSeed = seed;
    }

    /**
     * Sets the timing used as the delay for deletion. Used if you don't want a test to hang for 10 minutes
     * @param timeQuantity The amount of the specified unit to wait for
     * @param timeUnit The unit of time which {@code timeQuantity} refers to
     */
    public void setDelay(int timeQuantity, TimeUnit timeUnit) {
        this.timeQuantity = timeQuantity;
        this.timeUnit = timeUnit;
    }

    /**
     * Generates and saves a {@link VerificationCode}, schedules its deletion and puts this scheduled task in the map
     * @param generationStrategy The {@link GenerationStrategy} to be used for code generation.
     *                           Configures the domain and length of the code generated
     * @param user The user for which this code is issued
     * @param locale The locale from which this code was issued
     * @return The issued code as a {@code String}
     */
    public String issueSignupCode(GenerationStrategy generationStrategy, User user, Locale locale) {
        SecureRandomCodeGenerator secureRandomCodeGenerator = generationStrategy.getGenerator(randomSeed);
        String code = generateUniqueCode(secureRandomCodeGenerator);
        verificationCodeRepository.save(new VerificationCode(
                user,
                code,
                locale
        ));
        ScheduledFuture<?> scheduledDeletion = deletionExecutor.schedule(
                () -> deleteSignupCodeAndAccount(code),
                timeQuantity,
                timeUnit
        );
        deletionContractMap.put(code, scheduledDeletion);
        return code;
    }

    /**
     * Attempts to consume the specified signup code. If it is valid, the associated user account will be activated and the
     * {@link VerificationCode} will be deleted. If it is not valid or not present (expired), throws an exception.
     * If the code exists but is invalid, it is also deleted.
     * @param signupCode The signup code to be consumed
     * @throws IllegalArgumentException If the code is not valid or does not exist
     */
    public void consumeSignupCode(String signupCode) throws IllegalArgumentException {
        Optional<VerificationCode> verificationCodeOptional = verificationCodeRepository.findByCode(signupCode);
        if (verificationCodeOptional.isPresent()) {
            VerificationCode verificationCode = verificationCodeOptional.get();
            User user = verificationCode.getUser();
            if (verificationCodeValidation.isValid(verificationCode, signupCode)) {
                user.activate();
                userRepository.save(user);
                verificationCodeRepository.delete(verificationCode);
                return;
            } else {
                verificationCodeRepository.delete(verificationCode);
                throw new IllegalArgumentException("Signup code invalid");
            }
        }
        throw new IllegalArgumentException("Signup code invalid");
    }

    /**
     * Attempts to retrieve the {@link VerificationCode} that was issued. If it still exists and is expired, deletes the code
     * and the {@link User} which created it. The user should not be activated.
     * @param code The issued code as a {@code String}
     * @throws IllegalArgumentException if The {@link User} being deleted is activated
     */
    public void deleteSignupCodeAndAccount(String code) throws IllegalArgumentException {
        Optional<VerificationCode> verificationCodeOptional = verificationCodeRepository.findByCode(code);
        if (verificationCodeOptional.isPresent()) {
            VerificationCode verificationCode = verificationCodeOptional.get();
            User user = verificationCode.getUser();
            if (user.isActivated()) {
                throw new IllegalStateException("An expired signup code exists whose associated user is activated");
            }
            logger.info("deleting user whose signup code expired. User: {}, code: {}", user, code);
            verificationCodeRepository.delete(verificationCode);
            userRepository.delete(user);
        }
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
}

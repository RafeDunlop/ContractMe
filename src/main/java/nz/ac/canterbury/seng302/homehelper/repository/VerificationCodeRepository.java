package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.verificationCode;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationCodeRepository extends CrudRepository<verificationCode, Long> {

    Optional<verificationCode> findByCode(byte[] code);

    void deleteByExpiryTimeBefore(LocalDateTime time);
}

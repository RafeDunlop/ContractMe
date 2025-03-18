package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.VerificationCode;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationCodeRepository extends CrudRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByCode(String code);

    void deleteByExpiryTimeBefore(LocalDateTime time);
}

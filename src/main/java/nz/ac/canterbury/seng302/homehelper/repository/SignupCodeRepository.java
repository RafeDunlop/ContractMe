package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.SignupCode;
import org.springframework.data.repository.CrudRepository;

public interface SignupCodeRepository extends CrudRepository<SignupCode, Long> {
}

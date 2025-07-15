package nz.ac.canterbury.seng302.homehelper.repository.userReposoitories;

import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface UserBaseRepository<T extends User> extends CrudRepository<T, Long> {

    Optional<T> findByEmailIgnoreCase(String email);

}

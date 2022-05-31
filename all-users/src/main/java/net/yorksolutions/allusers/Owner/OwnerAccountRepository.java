package net.yorksolutions.allusers.Owner;

import net.yorksolutions.allusers.Customer.UserAccount;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repositories dictate the queries that we can run on our data
@Repository
public interface OwnerAccountRepository extends CrudRepository<OwnerAccount, Long> {
    Optional<UserAccount> findByUsernameAndPassword(String username, String password);

    Optional<UserAccount> findByUsername(String username);
}

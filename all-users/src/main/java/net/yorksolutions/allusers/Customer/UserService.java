package net.yorksolutions.allusers.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;

@Service
public class UserService {
    private UserAccountRepository repository;

    private HashMap<UUID, Long> tokenMap;

    @Autowired
    public UserService(@NonNull UserAccountRepository repository) {
        this.repository = repository;
        tokenMap = new HashMap<>();
    }

    public UserService(UserAccountRepository repository,
                       HashMap<UUID, Long> tokenMap) {
        this.repository = repository;
        this.tokenMap = tokenMap;
    }
}

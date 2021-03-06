package net.yorksolutions.allusers;

import net.yorksolutions.allusers.UserAccount;
import net.yorksolutions.allusers.UserAccountRepository;
import net.yorksolutions.allusers.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks

    @Spy
    UserService service;

    @Mock
    UserAccountRepository repository;

    @Mock
    HashMap<UUID, Long> tokenMap;

    @Test
    void itShouldReturnUnauthWhenUserIsWrong() {
        final var username = UUID.randomUUID().toString();
        final var password = UUID.randomUUID().toString();
        lenient().when(repository.findByUsernameAndPassword(username, password))
                .thenReturn(Optional.empty());
        lenient().when(repository.findByUsernameAndPassword(not(eq(username)), eq(password)))
                .thenReturn(Optional.of(new UserAccount()));
        assertThrows(ResponseStatusException.class, () -> service.login(username, password));
    }

    @Test
    void itShouldReturnUnauthWhenPassIsWrong() {
        final var username = UUID.randomUUID().toString();
        final var password = UUID.randomUUID().toString();
        lenient().when(repository.findByUsernameAndPassword(username, password))
                .thenReturn(Optional.empty());
        lenient().when(repository.findByUsernameAndPassword(eq(username), not(eq(password))))
                .thenReturn(Optional.of(new UserAccount()));
        assertThrows(ResponseStatusException.class, () -> service.login(username, password));
    }

    @Test
    void itShouldMapTheUUIDToTheIdWhenLoginSuccess() {
        final var username = UUID.randomUUID().toString();
        final var password = UUID.randomUUID().toString();
        final Long id = (long) (Math.random() * 9999999); // the id of the user account associated with username, password
        final UserAccount expected = new UserAccount();
        expected.id = id;
        expected.username = username;
        expected.password = password;
        when(repository.findByUsernameAndPassword(username, password))
                .thenReturn(Optional.of(expected));
        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        when(tokenMap.put(captor.capture(), eq(id))).thenReturn(0L);
        final var token = service.login(username, password);
        assertEquals(token, captor.getValue());
    }

    @Test
    void itShouldReturnInvalidIfUsernameExists() {
        final String username = "some username";
        when(repository.findByUsername(username)).thenReturn(Optional.of(
                new UserAccount()));
        assertThrows(ResponseStatusException.class, () -> service.register(username, ""));
    }

    @Test
    void itShouldSaveANewUserAccountWhenUserIsUnique() {
        final String username = "some username";
        final String password = "some password";
        when(repository.findByUsername(username)).thenReturn(Optional.empty());
        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        when(repository.save(captor.capture())).thenReturn(new UserAccount());
        Assertions.assertDoesNotThrow(() -> service.register(username, password));
        assertEquals(new UserAccount(), captor.getValue());
    }

    @Test
    void itShouldNotThrowWhenTokenIsCorrect() {
        final UUID token = UUID.randomUUID();
        when(tokenMap.containsKey(token)).thenReturn(true);
        assertDoesNotThrow(() -> service.isAuthorized(token));
    }

    @Test
    void itShouldThrowUnauthWhenTokenIsBad() {
        final UUID token = UUID.randomUUID();
        when(tokenMap.containsKey(token)).thenReturn(false);
        final ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.isAuthorized(token));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

}
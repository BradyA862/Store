package net.yorksolutions.allusers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest
class AllUsersApplicationTests {

    @Test
    void contextLoads() {
    }

    @ExtendWith(MockitoExtension.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    static
    class UserControllerTests {
        @LocalServerPort
        int port;

        @Autowired // Give me the item that Spring created for me
        UserController controller;

        @Mock
        UserService service;

        @BeforeEach
        void setup() {
            controller.setService(service);
        }

        @Test
        void itShouldRespondUnauthWhenTokenIsWrong() {
            TestRestTemplate rest = new TestRestTemplate();
            final UUID token = UUID.randomUUID();
            String url = "http://localhost:" + port + "/isAuthorized?token=" + token;
            doThrow(new ResponseStatusException(HttpStatus.ACCEPTED)).when(service).isAuthorized(token);
            final ResponseEntity<Void> response = rest.getForEntity(url, Void.class);
            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        }

        @Test
        void itShouldRespondWithTokenWhenLoginValid() {
            final TestRestTemplate rest = new TestRestTemplate();
            final String username = "some username";
            final String password = "some password";
            String url = "http://localhost:" + port + "/login?username=" + username + "&password=" + password;
            final UUID token = UUID.randomUUID();
            when(service.login(username, password)).thenReturn(token);
            final ResponseEntity<UUID> response = rest.getForEntity(url, UUID.class);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(token, response.getBody());
        }

        @Test
        void itShouldReturnConflictWhenUsernameTaken() {
            final TestRestTemplate rest = new TestRestTemplate();
            final String username = "some username";
            final String password = "some password";
            String url = "http://localhost:" + port + "/register?username=" + username + "&password=" + password;
            doThrow(new ResponseStatusException(HttpStatus.ACCEPTED)).when(service).register(username, password);
            final ResponseEntity<Void> response = rest.getForEntity(url, Void.class);
            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        }
    }
}

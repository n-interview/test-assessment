package com.test.testassessment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.test.testassessment.model.Token;
import com.test.testassessment.model.User;
import com.test.testassessment.repository.UserRepository;
import com.test.testassessment.service.impl.TokenService;
import com.test.testassessment.service.impl.UserServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenService tokenService;
    @InjectMocks
    private UserServiceImpl userService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SOME_TOKEN = "SOME_TOKEN";

    private static final String RAND_STRING = "RAND_STRING";

    private static final String DATA = "data";


    @BeforeAll
    private static void setMapperModules() {
        objectMapper.findAndRegisterModules();
    }

    @Test
    public void saveUserSuccess() {
        User userToReturn = getUser("1", "James Ditter", "jditter", null, null);

        when(userRepository.save(any(User.class))).thenReturn(userToReturn);

        userService.saveUser(userToReturn);

        Assertions.assertThat(userToReturn.getPassword()).isNotNull();
        Assertions.assertThat(userToReturn.getId()).isNotNull();
        Assertions.assertThat(userToReturn.getSalt()).isNotNull();

        Assertions.assertThat(userToReturn.getPassword().contentEquals(DigestUtils.sha256Hex(userToReturn.getSalt() + "J@me$5")));
    }

    @Test
    public void saveUserFailure() {

        User actualUser = userService.saveUser(null);

        Assertions.assertThat(actualUser).isNull();
    }

    @Test
    public void generateTokenSuccess() {
        User userToReturn = getUser("1", "James Ditter", "jditter", "ABC", "J@me$5");
        Token mockToken = new Token(SOME_TOKEN, ZonedDateTime.now().plusMinutes(5));

        when(userRepository.findById(any())).thenReturn(Optional.of(userToReturn));
        when(tokenService.generateToken(any())).thenReturn(mockToken);

        String token = userService.authenticateUserByIdAndPassword("1", "J@me$5");
        Assertions.assertThat(token).isNotNull();
        Assertions.assertThat(new String(Base64.getDecoder().decode(token))).isNotNull();

    }

    @Test
    public void generateTokenFailure() {
        User userToReturn = getUser("1", "James Ditter", "jditter", "ABC", "J@me$5");

        when(userRepository.findById(any())).thenReturn(Optional.of(userToReturn));
        when(tokenService.generateToken(any())).thenReturn(null);

        String token = userService.authenticateUserByIdAndPassword("1", "J@me$5");
        Assertions.assertThat(token).isNull();
    }

    @Test
    public void generateTokenWithoutUser() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        String token = userService.authenticateUserByIdAndPassword(null, null);
        Assertions.assertThat(token).isNull();
    }

    @Test
    public void getAllUsersWithResults() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(new User()));

        Assertions.assertThat(userService.getAllUsers()).isNotNull();
    }

    @Test
    public void getAllUsersNullResult() {
        when(userRepository.findAll()).thenReturn(null);

        Assertions.assertThat(userService.getAllUsers()).isNull();
    }

    @Test
    public void updateUserSuccess() {
        User user = getUser("1", "James Ditter", "jditter", "ABC", "J@me$5");

        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        User updatedUser = getUser("1", "James Ditar", "jditar", "ABC", "J@me$5");

        when(userRepository.save(updatedUser)).thenReturn(updatedUser);

        User actualUser = userService.updateUser(updatedUser.getId(), updatedUser);
        Assertions.assertThat(actualUser).isEqualTo(updatedUser);

    }

    @Test
    public void updateUserWithPasswordChangeSuccess() {
        User user = getUser("1", "James Ditter", "jditter", "ABC", "J@me$5");

        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        User updatedUser = getUser("1", "James Ditar", "jditar", "ABCD", "J@me$6");

        when(userRepository.save(updatedUser)).thenReturn(updatedUser);

        User actualUser = userService.updateUser(updatedUser.getId(), updatedUser);
        Assertions.assertThat(actualUser).isEqualTo(updatedUser);

    }

    @Test
    public void updateUserWithNoMatchingId() {
        User user = getUser("2", "James Ditter", "jditter", "ABC", "J@me$5");

        when(userRepository.findById("2")).thenReturn(Optional.empty());

        User updatedUser = userService.updateUser("2", user);

        Assertions.assertThat(updatedUser).isNull();

    }

    @Test
    public void validateTokenSuccess() throws JsonProcessingException {
        Token mockToken = new Token(SOME_TOKEN, ZonedDateTime.now().plusMinutes(5));
        String base64EncodedToken = Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(mockToken).getBytes());

        when(tokenService.isTokenValid(eq("1"), any())).thenReturn(true);

        Assertions.assertThat(userService.validateToken("1", base64EncodedToken)).isEqualTo(true);
    }

    @Test
    public void validateTokenFailture() throws JsonProcessingException {
        Token mockToken = new Token(SOME_TOKEN, ZonedDateTime.now().plusMinutes(5));
        String base64EncodedToken = Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(mockToken).getBytes());

        when(tokenService.isTokenValid(eq("1"), any())).thenReturn(false);

        Assertions.assertThat(userService.validateToken("1", base64EncodedToken)).isEqualTo(false);
    }

    @Test
    public void validateTokenInvalidToken() {
        when(tokenService.isTokenValid(any(), any())).thenReturn(false);

        Assertions.assertThat(userService.validateToken("1", RAND_STRING)).isEqualTo(false);
    }

    @Test
    public void revokeTokenSuccess() throws JsonProcessingException {
        Token mockToken = new Token(SOME_TOKEN, ZonedDateTime.now().plusMinutes(5));
        String base64EncodedToken = Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(mockToken).getBytes());

        when(tokenService.revokeToken(eq("1"), any())).thenReturn(true);

        Assertions.assertThat(userService.revokeToken("1", base64EncodedToken)).isEqualTo(true);
    }

    @Test
    public void revokeTokenFailure() throws JsonProcessingException {
        Token mockToken = new Token(SOME_TOKEN, ZonedDateTime.now().plusMinutes(5));
        String base64EncodedToken = Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(mockToken).getBytes());

        when(tokenService.revokeToken(eq("1"), any())).thenReturn(false);

        Assertions.assertThat(userService.revokeToken("1", base64EncodedToken)).isEqualTo(false);

    }

    @Test
    public void revokeTokenInvalidToken() {
        when(tokenService.revokeToken(any(), any())).thenReturn(false);

        Assertions.assertThat(userService.revokeToken("1", RAND_STRING)).isEqualTo(false);
    }

    @Test
    public void buildResponseSuccess() throws JsonProcessingException {
        String data = userService.buildResponse(RAND_STRING);
        ObjectNode parsedData = (ObjectNode) objectMapper.readTree(data);

        Assertions.assertThat(parsedData != null).isTrue();
        Assertions.assertThat(parsedData.get(DATA) != null).isTrue();
        Assertions.assertThat(parsedData.get(DATA).asText()).isEqualTo(RAND_STRING);
    }

    private static User getUser(String id, String fullName, String userName, String salt, String password) {
        User userToReturn = new User();
        userToReturn.setId(id);
        userToReturn.setFullName(fullName);
        userToReturn.setUserName(userName);
        userToReturn.setSalt(salt);
        userToReturn.setPassword(DigestUtils.sha256Hex(userToReturn.getSalt() + password));
        return userToReturn;
    }

}

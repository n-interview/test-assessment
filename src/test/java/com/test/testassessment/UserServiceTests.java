package com.test.testassessment;

import com.test.testassessment.model.Token;
import com.test.testassessment.model.User;
import com.test.testassessment.repository.UserRepository;
import com.test.testassessment.service.impl.TokenService;
import com.test.testassessment.service.impl.UserServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenService tokenService;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void saveUserSuccess() {
        User userToReturn = new User();
        userToReturn.setFullName("James Ditter");
        userToReturn.setUserName("jditter");
        userToReturn.setPassword("J@me$5");

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
        User userToReturn = new User();
        userToReturn.setId("1");
        userToReturn.setFullName("James Ditter");
        userToReturn.setUserName("jditter");
        userToReturn.setSalt("ABC");
        userToReturn.setPassword(DigestUtils.sha256Hex(userToReturn.getSalt() + "J@me$5"));
        Token mockToken = new Token("SOME_TOKEN", ZonedDateTime.now().plusMinutes(5));

        when(userRepository.findById(any())).thenReturn(Optional.of(userToReturn));
        when(tokenService.generateToken(any())).thenReturn(mockToken);

        String token = userService.authenticateUserByIdAndPassword("1", "J@me$5");
        Assertions.assertThat(token).isNotNull();
        Assertions.assertThat(new String(Base64.getDecoder().decode(token))).isNotNull();

    }

    @Test
    public void generateTokenFailture() {
        User userToReturn = new User();
        userToReturn.setId("1");
        userToReturn.setFullName("James Ditter");
        userToReturn.setUserName("jditter");
        userToReturn.setSalt("ABC");
        userToReturn.setPassword(DigestUtils.sha256Hex(userToReturn.getSalt() + "J@me$5"));

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

}

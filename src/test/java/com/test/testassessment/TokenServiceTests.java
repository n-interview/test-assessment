package com.test.testassessment;

import com.test.testassessment.model.Token;
import com.test.testassessment.model.User;
import com.test.testassessment.service.impl.TokenServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Map;

@SpringBootTest
public class TokenServiceTests {

    @Autowired
    private TokenServiceImpl tokenService;
    private static Map<String, Token> tokenCache;

    @BeforeEach
    public void setTokenCache() {
        tokenCache = (Map<String, Token>) ReflectionTestUtils.getField(tokenService, "tokenCache");
    }

    @Test
    public void generateTokenWithNoUser() {

        Token token = tokenService.generateToken(null);
        Assertions.assertThat(token).isNull();
        Assertions.assertThat(tokenCache.isEmpty());

    }

    @Test
    public void generateTokenForValidUser() {
        User user = new User();
        user.setId("1");
        user.setUserName("jsmith");

        String expectedTokenContent = Base64.getEncoder().encodeToString((user.getId() + "." + user.getUserName()).getBytes());

        Token token = tokenService.generateToken(user);
        Assertions.assertThat(token.getContent()).isEqualTo(expectedTokenContent);
        Assertions.assertThat(token.getExpiryDate()).isAfter(ZonedDateTime.now());
        Assertions.assertThat(tokenCache.get(user.getId())).isEqualTo(token);
    }

    @Test
    public void generateTokenForValidUserTwice() {
        User user = new User();
        user.setId("1");
        user.setUserName("jsmith");

        String expectedTokenContent = Base64.getEncoder().encodeToString((user.getId() + "." + user.getUserName()).getBytes());

        Token token1 = tokenService.generateToken(user);
        Token token2 = tokenService.generateToken(user);
        Assertions.assertThat(token1.getContent()).isEqualTo(expectedTokenContent);
        Assertions.assertThat(token1.getExpiryDate()).isAfter(ZonedDateTime.now());
        Assertions.assertThat(token2.getContent()).isEqualTo(expectedTokenContent);
        Assertions.assertThat(token2.getExpiryDate()).isAfter(ZonedDateTime.now());
        Assertions.assertThat(tokenCache.get(user.getId())).isEqualTo(token2);
        Assertions.assertThat(tokenCache.size()).isEqualTo(1);
        Assertions.assertThat(tokenCache.get(user.getId())).isNotEqualTo(token1);

    }

    @Test
    public void generateTokenAfterExpiry() {
        User user = new User();
        user.setId("1");
        user.setUserName("jsmith");

        String expectedTokenContent = Base64.getEncoder().encodeToString((user.getId() + "." + user.getUserName()).getBytes());

        Token token = tokenService.generateToken(user);
        Assertions.assertThat(tokenCache.get(user.getId())).isNotNull();

        token.setExpiryDate(ZonedDateTime.now().minusYears(1));
        tokenCache.put(user.getId(), token);
        ReflectionTestUtils.setField(tokenService, "tokenCache", tokenCache);

        Token token2 = tokenService.generateToken(user);
        Assertions.assertThat(tokenCache.get(user.getId()).getExpiryDate()).isAfter(ZonedDateTime.now());
    }

    @Test
    public void isTokenValidSuccess() {
        String tokenContent = Base64.getEncoder().encodeToString("1.jsmith".getBytes());
        Token token = new Token(tokenContent, ZonedDateTime.now().plusMinutes(5));

        tokenCache.put("1", token);
        ReflectionTestUtils.setField(tokenService, "tokenCache", tokenCache);

        Assertions.assertThat(tokenService.isTokenValid("1", token)).isTrue();
    }

    @Test
    public void isTokenValidNullToken() {
        Assertions.assertThat(tokenService.isTokenValid(null, null)).isEqualTo(false);
    }

    @Test
    public void isTokenValidNullUser() {
        String tokenContent = Base64.getEncoder().encodeToString("test".getBytes());
        Assertions.assertThat(tokenService.isTokenValid(null, new Token(tokenContent, ZonedDateTime.now()))).isFalse();

    }

    @Test
    public void isTokenValidNullUserNullUserInToken() {
        String tokenContent = Base64.getEncoder().encodeToString("test".getBytes());
        Assertions.assertThat(tokenService.isTokenValid("1", new Token(tokenContent, ZonedDateTime.now()))).isFalse();

    }

    @Test
    public void isTokenValidNullUserTokenNotInCache() {
        String tokenContent = Base64.getEncoder().encodeToString("1.jsmith".getBytes());
        Token token = new Token(tokenContent, ZonedDateTime.now().plusMinutes(5));

        Assertions.assertThat(tokenService.isTokenValid(null, token)).isFalse();

    }

    @Test
    public void revokeTokenSuccess() {
        String tokenContent = Base64.getEncoder().encodeToString("1.jsmith".getBytes());
        Token token = new Token(tokenContent, ZonedDateTime.now().plusMinutes(5));

        tokenCache.put("1", token);
        ReflectionTestUtils.setField(tokenService, "tokenCache", tokenCache);

        Assertions.assertThat(tokenService.revokeToken("1", token)).isTrue();
        Assertions.assertThat(tokenCache.size()).isEqualTo(0);
    }

    @Test
    public void revokeTokenNullToken() {
        Assertions.assertThat(tokenService.revokeToken(null, null)).isFalse();
    }

    @Test
    public void revokeTokenDifferentUserIds() {
        String tokenContent = Base64.getEncoder().encodeToString("1.jsmith".getBytes());
        Token token = new Token(tokenContent, ZonedDateTime.now().plusMinutes(5));

        Assertions.assertThat(tokenService.revokeToken("2", token)).isFalse();
    }


}

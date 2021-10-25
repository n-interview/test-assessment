package com.test.testassessment;

import com.test.testassessment.model.Token;
import com.test.testassessment.model.User;
import com.test.testassessment.service.impl.TokenServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    private static final String TOKEN_CACHE = "tokenCache";

    private static final String DELIMITER = ".";

    private static final String TEST_TOKEN = "1.jsmith";

    private static final String TEST_STRING = "test";

    @BeforeEach
    public void setTokenCache() {
        tokenCache = (Map<String, Token>) ReflectionTestUtils.getField(tokenService, TOKEN_CACHE);
    }

    @Test
    public void generateTokenWithNoUser() {

        Token token = tokenService.generateToken(null);
        Assertions.assertThat(token).isNull();
        Assertions.assertThat(tokenCache.isEmpty());

    }

    @Test
    public void generateTokenForValidUser() {
        User user = getTestUser();

        String expectedTokenContent = Base64.getEncoder().encodeToString((user.getId() + DELIMITER + user.getUserName()).getBytes());

        Token token = tokenService.generateToken(user);
        Assertions.assertThat(token.getContent()).isEqualTo(expectedTokenContent);
        Assertions.assertThat(token.getExpiryDate()).isAfter(ZonedDateTime.now());
        Assertions.assertThat(tokenCache.get(user.getId())).isEqualTo(token);
    }

    @Test
    public void generateTokenForValidUserTwice() {
        User user = getTestUser();

        String expectedTokenContent = Base64.getEncoder().encodeToString((user.getId() + DELIMITER + user.getUserName()).getBytes());

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
        User user = getTestUser();

        String expectedTokenContent = Base64.getEncoder().encodeToString((user.getId() + DELIMITER + user.getUserName()).getBytes());

        Token token = tokenService.generateToken(user);
        Assertions.assertThat(tokenCache.get(user.getId())).isNotNull();

        token.setExpiryDate(ZonedDateTime.now().minusYears(1));
        tokenCache.put(user.getId(), token);
        ReflectionTestUtils.setField(tokenService, TOKEN_CACHE, tokenCache);

        Token token2 = tokenService.generateToken(user);
        Assertions.assertThat(tokenCache.get(user.getId()).getExpiryDate()).isAfter(ZonedDateTime.now());
    }

    @Test
    public void isTokenValidSuccess() {
        String tokenContent = Base64.getEncoder().encodeToString(TEST_TOKEN.getBytes());
        Token token = new Token(tokenContent, ZonedDateTime.now().plusMinutes(5));

        tokenCache.put("1", token);
        ReflectionTestUtils.setField(tokenService, TOKEN_CACHE, tokenCache);

        Assertions.assertThat(tokenService.isTokenValid("1", token)).isTrue();
    }

    @Test
    public void isTokenValidNullToken() {
        Assertions.assertThat(tokenService.isTokenValid(null, null)).isEqualTo(false);
    }

    @Test
    public void isTokenValidNullUser() {
        String tokenContent = Base64.getEncoder().encodeToString(TEST_STRING.getBytes());
        Assertions.assertThat(tokenService.isTokenValid(null, new Token(tokenContent, ZonedDateTime.now()))).isFalse();

    }

    @Test
    public void isTokenValidNullUserNullUserInToken() {
        String tokenContent = Base64.getEncoder().encodeToString(TEST_STRING.getBytes());
        Assertions.assertThat(tokenService.isTokenValid("1", new Token(tokenContent, ZonedDateTime.now()))).isFalse();

    }

    @Test
    public void isTokenValidNullUserTokenNotInCache() {
        String tokenContent = Base64.getEncoder().encodeToString(TEST_TOKEN.getBytes());
        Token token = new Token(tokenContent, ZonedDateTime.now().plusMinutes(5));

        Assertions.assertThat(tokenService.isTokenValid(null, token)).isFalse();

    }

    @Test
    public void revokeTokenSuccess() {
        String tokenContent = Base64.getEncoder().encodeToString(TEST_TOKEN.getBytes());
        Token token = new Token(tokenContent, ZonedDateTime.now().plusMinutes(5));

        tokenCache.put("1", token);
        ReflectionTestUtils.setField(tokenService, TOKEN_CACHE, tokenCache);

        Assertions.assertThat(tokenService.revokeToken("1", token)).isTrue();
        Assertions.assertThat(tokenCache.size()).isEqualTo(0);
    }

    @Test
    public void revokeTokenNullToken() {
        Assertions.assertThat(tokenService.revokeToken(null, null)).isFalse();
    }

    @Test
    public void revokeTokenDifferentUserIds() {
        String tokenContent = Base64.getEncoder().encodeToString(TEST_TOKEN.getBytes());
        Token token = new Token(tokenContent, ZonedDateTime.now().plusMinutes(5));

        Assertions.assertThat(tokenService.revokeToken("2", token)).isFalse();
    }

    private static User getTestUser() {
        User user = new User();
        user.setId("1");
        user.setUserName("jsmith");
        return user;
    }

}

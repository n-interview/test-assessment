package com.test.testassessment.service.impl;

import com.test.testassessment.model.Token;
import com.test.testassessment.model.User;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenServiceImpl implements TokenService {

    private static final String DELIMITER = ".";
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    @Value("${test.assessment.token.expiry.minutes:5}")
    private String tokenDuration;
    private Map<String, Token> tokenCache = new HashMap<>(); // keyed by userId

    @Override
    public Token generateToken(User user) {
        if (user == null) {
            return null;
        }
        Token token = new Token(generateTokenContent(user), ZonedDateTime.now().plusMinutes(Long.valueOf(tokenDuration)));
        if (tokenCache.containsKey(user.getId())) {
            // this might need to raise an error depending on future needs or how clients handle token generation
            log.info("User {} already present in cache when generating a new token. Proceeding with generating a new token", user.getId());
            if (!isTokenValid(null, tokenCache.get(user.getId()))) {
                tokenCache.put(user.getId(), token);
            }
        }
        tokenCache.put(user.getId(), token);
        return token;
    }

    @Override
    public boolean isTokenValid(String userId, Token token) {
        if (token == null) {
            return false;
        }
        String userIdFromToken = getUserIdFromToken(token);
        if (userId != null) {
            if (!StringUtils.equals(userId, userIdFromToken)) {
                return false;
            }
        }
        if (userIdFromToken == null) {
            return false;
        } else {
            if (!tokenCache.containsKey(userIdFromToken)) {
                return false;
            }
            if (isTokenExpired(tokenCache.get(userIdFromToken))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean revokeToken(String userId, Token token) {
        if (token == null) {
            return false;
        }
        String userIdFromToken = getUserIdFromToken(token);
        if (!StringUtils.equals(userId, userIdFromToken)) {
            return false;
        }
        if (userIdFromToken != null) {
            if (tokenCache.get(userIdFromToken) != null) {
                tokenCache.remove(userIdFromToken);
                return true;
            }
        }
        return false;
    }

    private String generateTokenContent(User user) {
        if (user == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString((user.getId() + DELIMITER + user.getUserName()).getBytes());
    }

    private boolean isTokenExpired(Token token) {
        if (token == null) {
            return true;
        }
        return ZonedDateTime.now().isAfter(token.getExpiryDate());
    }

    private String getUserIdFromToken(Token token) {
        if (token == null) {
            return null;
        }
        String userId = null;
        String tokenContent = token.getContent();
        if (tokenContent != null) {
            String[] splitToken = new String(Base64.getDecoder().decode(tokenContent)).split("\\" + DELIMITER);
            if (splitToken.length == 2) {
                userId = splitToken[0];
            }
            if (userId != null) {
                return userId;
            }
        }
        return null;
    }

}

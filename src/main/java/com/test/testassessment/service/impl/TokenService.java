package com.test.testassessment.service.impl;

import com.test.testassessment.model.Token;
import com.test.testassessment.model.User;
import org.springframework.stereotype.Service;

@Service
public interface TokenService {

    Token generateToken(User user);

    boolean isTokenValid(String userId, Token token);

    boolean revokeToken(String userId, Token token);

}

package com.test.testassessment.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.test.testassessment.model.Token;
import com.test.testassessment.model.User;
import com.test.testassessment.repository.UserRepository;
import com.test.testassessment.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final String DATA_FIELD_NAME = "data";
    private UserRepository userRepository;
    private TokenService tokenService;


    public UserServiceImpl(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User saveUser(User user) {
        if (user == null || StringUtils.isEmpty(user.getUserName()) || StringUtils.isEmpty(user.getPassword())) {
            return null;
        }
        String salt = generateSalt();
        user.setId(UUID.randomUUID().toString());
        user.setSalt(salt);
        user.setPassword(hashPassword(salt, user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User updateUser(String userId, User user) {
        Optional<User> oldUser = userRepository.findById(userId);
        if (oldUser.isPresent()) {
            User userToUpdate = oldUser.get();
            if (!StringUtils.equals(hashPassword(userToUpdate.getSalt(), userToUpdate.getPassword()), hashPassword(userToUpdate.getSalt(), user.getPassword()))) {
                user.setPassword(hashPassword(userToUpdate.getSalt(), user.getPassword()));
            }
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public String authenticateUserByIdAndPassword(String userId, String password) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User actualUser = user.get();
            if (StringUtils.equals(actualUser.getPassword(), hashPassword(actualUser.getSalt(), password))) {
                Token token = tokenService.generateToken(actualUser);
                try {
                    if (token != null) {
                        return Base64.getEncoder().encodeToString(mapper.writeValueAsString(token).getBytes());
                    }
                    return null;
                } catch (JsonProcessingException | IllegalArgumentException e) {
                    log.error("Could not authenticate user {}, exception ", userId, e);
                }
            }
        }
        log.info("User {} could not be authenticated: user not found", userId);
        return null;
    }

    @Override
    public boolean validateToken(String userId, String token) {
        Token decodedToken = null;
        try {
            decodedToken = mapper.readValue(new String(Base64.getDecoder().decode(token)), Token.class);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            log.error("Exception while validating token ", e);
        }
        if (tokenService.isTokenValid(userId, decodedToken)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean revokeToken(String userId, String token) {
        try {
            Token decodedToken = mapper.readValue(new String(Base64.getDecoder().decode(token)), Token.class);
            if (tokenService.revokeToken(userId, decodedToken)) {
                return true;
            }
            return false;
        } catch (JsonProcessingException | IllegalArgumentException e) {
            log.error("Exception while revoking token ", e);
        }
        return false;
    }

    @Override
    public String buildResponse(String dataToSerialize) {
        ObjectNode data = mapper.createObjectNode();
        try {
            data.put(DATA_FIELD_NAME, dataToSerialize);
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("Error while building response ", e);
        }
        return null;
    }

    private String generateSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte bytes[] = new byte[32];
        secureRandom.nextBytes(bytes);
        return new String(Base64.getEncoder().encode(bytes));
    }

    private String hashPassword(String salt, String password) {
        return DigestUtils.sha256Hex(salt + password);
    }

}

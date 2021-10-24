package com.test.testassessment.service;

import com.test.testassessment.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    List<User> getAllUsers();

    User saveUser(User user);

    User updateUser(User user);

    String authenticateUserByIdAndPassword(String userName, String password);

    boolean validateToken(String userId, String token);

    boolean revokeToken(String userId, String token);

    String buildResponse(String dataToSerialize);

}

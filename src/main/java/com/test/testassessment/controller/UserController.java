package com.test.testassessment.controller;

import com.test.testassessment.model.User;
import com.test.testassessment.service.UserService;
import com.test.testassessment.service.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers(@RequestHeader(name = "Authorization") String token) {
        log.debug("Getting all users");
        if (userService.validateToken(null, token)) { // nulling userId since there is no RBAC in this version
            return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/users")
    public ResponseEntity<User> addUser(@RequestBody User user) {
        log.debug("Adding user {}", user);
        User userToReturn = userService.saveUser(user);
        if (user != null) {
            return new ResponseEntity<>(userToReturn, HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user, @RequestHeader(name = "Authorization") String token) {
        log.debug("Updating user {}", id);
        if (!userService.validateToken(id, token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        User updateResult = userService.updateUser(id, user);
        if (updateResult != null) {
            return new ResponseEntity<>(updateResult, HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/users/{id}/token")
    public ResponseEntity<String> getTokenForUser(@PathVariable String id, @RequestBody User user) {
        log.debug("Getting token for user id {}", id);
        String tokenInBase64 = userService.authenticateUserByIdAndPassword(id, user.getPassword());
        if (tokenInBase64 != null) {
            return new ResponseEntity<>(userService.buildResponse(tokenInBase64), HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/users/{id}/validate-token")
    public ResponseEntity<String> validateTokenForUser(@PathVariable String id, @RequestHeader(name = "Authorization") String token) {
        log.debug("Validating token for user id {}", id);
        boolean response = userService.validateToken(id, token);
        if (response == true) {
            if (userService.buildResponse(String.valueOf(true)) != null) {
                return new ResponseEntity<>(userService.buildResponse(String.valueOf(true)), HttpStatus.ACCEPTED);
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

    }

    @PostMapping("/users/{id}/revoke-token")
    public ResponseEntity<String> revokeTokenForUser(@PathVariable String id, @RequestHeader(name = "Authorization") String token) {
        log.debug("Revoking token for user id {}", id);
        if (!userService.validateToken(id, token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        boolean response = userService.revokeToken(id, token);
        if (response == true) {
            if (userService.buildResponse(String.valueOf(true)) != null) {
                return new ResponseEntity<>(userService.buildResponse(String.valueOf(true)), HttpStatus.ACCEPTED);
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

}

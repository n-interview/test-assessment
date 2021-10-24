package com.test.testassessment.util;

import com.test.testassessment.model.User;
import com.test.testassessment.service.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements ApplicationRunner {

    private UserService userService;

    public DataLoader(UserService userService) {
        this.userService = userService;
    }

    public void run(ApplicationArguments args) {
        User user1 = new User();
        User user2 = new User();
        User user3 = new User();

        user1.setFullName("John Smith");
        user1.setUserName("jsmith");
        user1.setPassword("SomePass");

        user2.setFullName("Jane Doe");
        user2.setUserName("jdoe");
        user2.setPassword("QUjAskXl33$!");

        user3.setFullName("Timothy H");
        user3.setUserName("timh");
        user3.setPassword("Changeme!");

        userService.saveUser(user1);
        userService.saveUser(user2);
        userService.saveUser(user3);


    }

}

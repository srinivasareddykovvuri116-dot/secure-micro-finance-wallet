package com.finance.wallet.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserControllerTest {

    private final UserController userController = new UserController();

    @Test
    void userOnly_shouldReturnAccessGrantedMessage() {

        String response = userController.userOnly();

        assertEquals(
                "USER access granted",
                response
        );
    }
}
package com.finance.wallet.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestControllerTest {

    private final TestController testController = new TestController();

    @Test
    void test_shouldReturnSuccessMessage() {

        String response = testController.test();

        assertEquals(
                "JWT authentication successful",
                response
        );
    }
}
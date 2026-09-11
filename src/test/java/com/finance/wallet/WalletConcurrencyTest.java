package com.finance.wallet;

import com.finance.wallet.entity.User;
import com.finance.wallet.entity.Wallet;
import com.finance.wallet.repository.UserRepository;
import com.finance.wallet.repository.WalletRepository;
import com.finance.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WalletConcurrencyTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void concurrentWithdrawalsShouldNotOverspendWallet() throws Exception {

        // Create a test user
        User user = new User();
        user.setEmail("concurrency-test@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setFullName("Concurrency Test User");

        User savedUser = userRepository.save(user);

        // Create wallet with ₹100
        Wallet wallet = new Wallet();
        wallet.setUser(savedUser);
        wallet.setBalance(new BigDecimal("100.00"));

        walletRepository.save(wallet);

        Long userId = savedUser.getId();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Boolean> withdrawal = () -> {
            try {
                walletService.withdraw(
                        userId,
                        new BigDecimal("80.00")
                );
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        };

        Future<Boolean> first = executor.submit(withdrawal);
        Future<Boolean> second = executor.submit(withdrawal);

        boolean firstResult = first.get();
        boolean secondResult = second.get();

        executor.shutdown();

        // Exactly one withdrawal must succeed
        assertTrue(firstResult ^ secondResult);

        Wallet finalWallet = walletRepository
                .findByUserId(userId)
                .orElseThrow();

        // ₹100 - ₹80 = ₹20
        assertEquals(
                new BigDecimal("20.00"),
                finalWallet.getBalance()
        );
    }
}
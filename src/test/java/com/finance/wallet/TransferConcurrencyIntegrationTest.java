package com.finance.wallet;

import com.finance.wallet.entity.Transaction;
import com.finance.wallet.entity.User;
import com.finance.wallet.entity.Wallet;
import com.finance.wallet.repository.TransactionRepository;
import com.finance.wallet.repository.UserRepository;
import com.finance.wallet.repository.WalletRepository;
import com.finance.wallet.service.TransferService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TransferConcurrencyIntegrationTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private User sender;
    private User receiver;

    @BeforeEach
    void setUp() {

        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        sender = new User();
        sender.setEmail("concurrency-sender@test.com");
        sender.setPasswordHash("test");
        sender.setFullName("Concurrency Sender");
        sender.setRole("ROLE_USER");
        sender.setActive(true);

        receiver = new User();
        receiver.setEmail("concurrency-receiver@test.com");
        receiver.setPasswordHash("test");
        receiver.setFullName("Concurrency Receiver");
        receiver.setRole("ROLE_USER");
        receiver.setActive(true);

        sender = userRepository.save(sender);
        receiver = userRepository.save(receiver);

        Wallet senderWallet = new Wallet();
        senderWallet.setUser(sender);
        senderWallet.setBalance(new BigDecimal("100.00"));

        Wallet receiverWallet = new Wallet();
        receiverWallet.setUser(receiver);
        receiverWallet.setBalance(new BigDecimal("0.00"));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);
    }

    @Test
    void concurrentTransfersShouldPreventDoubleSpending()
            throws Exception {

        ExecutorService executorService =
                Executors.newFixedThreadPool(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        Future<String> transferOne =
                executorService.submit(() -> {

                    startLatch.await();

                    try {
                        transferService.transfer(
                                sender.getId(),
                                receiver.getEmail(),
                                new BigDecimal("80.00")
                        );

                        return "SUCCESS";

                    } catch (IllegalArgumentException exception) {
                        return "FAILED";
                    }
                });

        Future<String> transferTwo =
                executorService.submit(() -> {

                    startLatch.await();

                    try {
                        transferService.transfer(
                                sender.getId(),
                                receiver.getEmail(),
                                new BigDecimal("80.00")
                        );

                        return "SUCCESS";

                    } catch (IllegalArgumentException exception) {
                        return "FAILED";
                    }
                });

        // Start both transfers at nearly the same time.
        startLatch.countDown();

        String resultOne = transferOne.get();
        String resultTwo = transferTwo.get();

        executorService.shutdown();

        int successfulTransfers = 0;

        if ("SUCCESS".equals(resultOne)) {
            successfulTransfers++;
        }

        if ("SUCCESS".equals(resultTwo)) {
            successfulTransfers++;
        }

        // Only one ₹80 transfer can succeed from a ₹100 balance.
        assertEquals(1, successfulTransfers);

        Wallet finalSenderWallet =
                walletRepository.findByUserId(sender.getId())
                        .orElseThrow();

        Wallet finalReceiverWallet =
                walletRepository.findByUserId(receiver.getId())
                        .orElseThrow();

        assertEquals(
                new BigDecimal("20.00"),
                finalSenderWallet.getBalance()
        );

        assertEquals(
                new BigDecimal("80.00"),
                finalReceiverWallet.getBalance()
        );

        List<Transaction> senderTransactions =
                transactionRepository
                        .findByWalletUserId(
                                sender.getId(),
                                org.springframework.data.domain.Pageable.unpaged()
                        )
                        .getContent();

        List<Transaction> receiverTransactions =
                transactionRepository
                        .findByWalletUserId(
                                receiver.getId(),
                                org.springframework.data.domain.Pageable.unpaged()
                        )
                        .getContent();

        // One successful transfer creates two ledger records.
        assertEquals(1, senderTransactions.size());
        assertEquals(1, receiverTransactions.size());
    }
}
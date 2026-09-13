package com.finance.wallet;

import com.finance.wallet.entity.Transaction;
import com.finance.wallet.entity.TransactionStatus;
import com.finance.wallet.entity.TransactionType;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class TransferIntegrationTest {

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

        sender = new User();
        sender.setEmail("sender@test.com");
        sender.setPasswordHash("test");
        sender.setFullName("Sender");
        sender.setRole("ROLE_USER");
        sender.setActive(true);

        receiver = new User();
        receiver.setEmail("receiver@test.com");
        receiver.setPasswordHash("test");
        receiver.setFullName("Receiver");
        receiver.setRole("ROLE_USER");
        receiver.setActive(true);

        sender = userRepository.save(sender);
        receiver = userRepository.save(receiver);

        Wallet senderWallet = new Wallet();
        senderWallet.setUser(sender);
        senderWallet.setBalance(new BigDecimal("100.00"));

        Wallet receiverWallet = new Wallet();
        receiverWallet.setUser(receiver);
        receiverWallet.setBalance(new BigDecimal("50.00"));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);
    }

    @Test
    void transferShouldUpdateBothWalletsAndCreateTransactions() {

        String referenceId = transferService.transfer(
                sender.getId(),
                receiver.getEmail(),
                new BigDecimal("30.00")
        );

        Wallet updatedSenderWallet =
                walletRepository.findByUserId(sender.getId())
                        .orElseThrow();

        Wallet updatedReceiverWallet =
                walletRepository.findByUserId(receiver.getId())
                        .orElseThrow();

        assertEquals(
                new BigDecimal("70.00"),
                updatedSenderWallet.getBalance()
        );

        assertEquals(
                new BigDecimal("80.00"),
                updatedReceiverWallet.getBalance()
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

        assertEquals(1, senderTransactions.size());
        assertEquals(1, receiverTransactions.size());

        assertEquals(
                TransactionType.TRANSFER_SENT,
                senderTransactions.get(0).getType()
        );

        assertEquals(
                TransactionType.TRANSFER_RECEIVED,
                receiverTransactions.get(0).getType()
        );

        assertEquals(
                TransactionStatus.SUCCESS,
                senderTransactions.get(0).getStatus()
        );

        assertEquals(
                TransactionStatus.SUCCESS,
                receiverTransactions.get(0).getStatus()
        );

        assertEquals(
                referenceId,
                senderTransactions.get(0).getReferenceId()
        );

        assertEquals(
                referenceId,
                receiverTransactions.get(0).getReferenceId()
        );
    }

    @Test
        void transferShouldNotPersistChangesWhenBalanceIsInsufficient() {

        Wallet senderWallet =
                walletRepository.findByUserId(sender.getId())
                        .orElseThrow();

        senderWallet.setBalance(new BigDecimal("20.00"));
        walletRepository.save(senderWallet);

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(
                        sender.getId(),
                        receiver.getEmail(),
                        new BigDecimal("30.00")
                )
        );

        Wallet updatedSenderWallet =
                walletRepository.findByUserId(sender.getId())
                        .orElseThrow();

        Wallet updatedReceiverWallet =
                walletRepository.findByUserId(receiver.getId())
                        .orElseThrow();

        assertEquals(
                new BigDecimal("20.00"),
                updatedSenderWallet.getBalance()
        );

        assertEquals(
                new BigDecimal("50.00"),
                updatedReceiverWallet.getBalance()
        );

        assertEquals(
                0,
                transactionRepository
                        .findByWalletUserId(
                                sender.getId(),
                                org.springframework.data.domain.Pageable.unpaged()
                        )
                        .getContent()
                        .size()
        );

        assertEquals(
                0,
                transactionRepository
                        .findByWalletUserId(
                                receiver.getId(),
                                org.springframework.data.domain.Pageable.unpaged()
                        )
                        .getContent()
                        .size()
        );
        }
}
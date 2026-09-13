package com.finance.wallet;

import com.finance.wallet.entity.Transaction;
import com.finance.wallet.entity.TransactionStatus;
import com.finance.wallet.entity.TransactionType;
import com.finance.wallet.entity.User;
import com.finance.wallet.entity.Wallet;
import com.finance.wallet.exception.ResourceNotFoundException;
import com.finance.wallet.repository.TransactionRepository;
import com.finance.wallet.repository.UserRepository;
import com.finance.wallet.repository.WalletRepository;
import com.finance.wallet.service.AuditLogService;
import com.finance.wallet.service.TransferService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class TransferServiceTest {

    private TransferService transferService;

    private UserRepository userRepository;
    private WalletRepository walletRepository;
    private TransactionRepository transactionRepository;
    private AuditLogService auditLogService;

    private User user;

    @BeforeEach
    void setUp() {

        userRepository = mock(UserRepository.class);
        walletRepository = mock(WalletRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        auditLogService = mock(AuditLogService.class);

        transferService = new TransferService(
                userRepository,
                walletRepository,
                transactionRepository,
                auditLogService
        );

        user = new User();
        user.setId(1L);
        user.setEmail("sender@example.com");
        user.setRole("ROLE_USER");
        user.setActive(true);
    }

    @Test
    void transferShouldSucceed() {

        User sender = new User();
        sender.setId(1L);
        sender.setEmail("sender@example.com");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setEmail("receiver@example.com");

        Wallet senderWallet = new Wallet();
        senderWallet.setUser(sender);
        senderWallet.setBalance(new BigDecimal("100.00"));

        Wallet receiverWallet = new Wallet();
        receiverWallet.setUser(receiver);
        receiverWallet.setBalance(new BigDecimal("50.00"));

        when(userRepository.findByEmail("receiver@example.com"))
                .thenReturn(Optional.of(receiver));

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUserIdForUpdate(2L))
                .thenReturn(Optional.of(receiverWallet));

        String referenceId = transferService.transfer(
                1L,
                "receiver@example.com",
                new BigDecimal("30.00")
        );

        assertNotNull(referenceId);

        assertEquals(
                new BigDecimal("70.00"),
                senderWallet.getBalance()
        );

        assertEquals(
                new BigDecimal("80.00"),
                receiverWallet.getBalance()
        );

        verify(transactionRepository, times(2))
                .save(any(Transaction.class));

        verify(auditLogService).log(
                sender,
                "TRANSFER",
                "amount=30.00, receiver=receiver@example.com"
        );
    }

    @Test
    void successfulTransferShouldCreateMatchingTransactionReferenceIds() {

        User sender = new User();
        sender.setId(1L);
        sender.setEmail("sender@example.com");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setEmail("receiver@example.com");

        Wallet senderWallet = new Wallet();
        senderWallet.setUser(sender);
        senderWallet.setBalance(new BigDecimal("100.00"));

        Wallet receiverWallet = new Wallet();
        receiverWallet.setUser(receiver);
        receiverWallet.setBalance(new BigDecimal("50.00"));

        when(userRepository.findByEmail("receiver@example.com"))
                .thenReturn(Optional.of(receiver));

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUserIdForUpdate(2L))
                .thenReturn(Optional.of(receiverWallet));

        transferService.transfer(
                1L,
                "receiver@example.com",
                new BigDecimal("25.00")
        );

        var transactionCaptor =
                org.mockito.ArgumentCaptor.forClass(Transaction.class);

        verify(transactionRepository, times(2))
                .save(transactionCaptor.capture());

        List<Transaction> transactions =
                transactionCaptor.getAllValues();

        Transaction sentTransaction = transactions.stream()
                .filter(transaction ->
                        transaction.getType()
                                == TransactionType.TRANSFER_SENT)
                .findFirst()
                .orElseThrow();

        Transaction receivedTransaction = transactions.stream()
                .filter(transaction ->
                        transaction.getType()
                                == TransactionType.TRANSFER_RECEIVED)
                .findFirst()
                .orElseThrow();

        assertEquals(
                sentTransaction.getReferenceId(),
                receivedTransaction.getReferenceId()
        );

        assertEquals(
                TransactionStatus.SUCCESS,
                sentTransaction.getStatus()
        );

        assertEquals(
                TransactionStatus.SUCCESS,
                receivedTransaction.getStatus()
        );

        assertEquals(
                new BigDecimal("25.00"),
                sentTransaction.getAmount()
        );

        assertEquals(
                new BigDecimal("25.00"),
                receivedTransaction.getAmount()
        );
    }

    @Test
    void transferShouldLockWalletsInUserIdOrderWhenSenderHasHigherId() {

        User sender = new User();
        sender.setId(5L);
        sender.setEmail("sender@example.com");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setEmail("receiver@example.com");

        Wallet senderWallet = new Wallet();
        senderWallet.setUser(sender);
        senderWallet.setBalance(new BigDecimal("100.00"));

        Wallet receiverWallet = new Wallet();
        receiverWallet.setUser(receiver);
        receiverWallet.setBalance(new BigDecimal("50.00"));

        when(userRepository.findByEmail("receiver@example.com"))
                .thenReturn(Optional.of(receiver));

        when(walletRepository.findByUserIdForUpdate(2L))
                .thenReturn(Optional.of(receiverWallet));

        when(walletRepository.findByUserIdForUpdate(5L))
                .thenReturn(Optional.of(senderWallet));

        transferService.transfer(
                5L,
                "receiver@example.com",
                new BigDecimal("20.00")
        );

        var inOrder = inOrder(walletRepository);

        inOrder.verify(walletRepository)
                .findByUserIdForUpdate(2L);

        inOrder.verify(walletRepository)
                .findByUserIdForUpdate(5L);
    }

    @Test
    void transferShouldFailWhenAmountIsZero() {

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(
                        1L,
                        "receiver@example.com",
                        BigDecimal.ZERO
                )
        );

        verify(userRepository, never())
                .findByEmail(anyString());

        verify(walletRepository, never())
                .findByUserIdForUpdate(anyLong());

        verify(transactionRepository, never())
                .save(any());

        verify(auditLogService, never())
                .log(any(), anyString(), anyString());
    }

    @Test
    void transferShouldFailWhenAmountIsNegative() {

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(
                        1L,
                        "receiver@example.com",
                        new BigDecimal("-10.00")
                )
        );

        verify(userRepository, never())
                .findByEmail(anyString());

        verify(walletRepository, never())
                .findByUserIdForUpdate(anyLong());

        verify(transactionRepository, never())
                .save(any());

        verify(auditLogService, never())
                .log(any(), anyString(), anyString());
    }

    @Test
    void transferShouldFailWhenBalanceIsInsufficient() {

        User receiver = new User();
        receiver.setId(2L);
        receiver.setEmail("receiver@example.com");

        Wallet senderWallet = new Wallet();
        senderWallet.setBalance(new BigDecimal("50.00"));

        Wallet receiverWallet = new Wallet();
        receiverWallet.setBalance(new BigDecimal("0.00"));

        when(userRepository.findByEmail("receiver@example.com"))
                .thenReturn(Optional.of(receiver));

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUserIdForUpdate(2L))
                .thenReturn(Optional.of(receiverWallet));

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(
                        1L,
                        "receiver@example.com",
                        new BigDecimal("80.00")
                )
        );

        assertEquals(
                new BigDecimal("50.00"),
                senderWallet.getBalance()
        );

        assertEquals(
                new BigDecimal("0.00"),
                receiverWallet.getBalance()
        );

        verify(transactionRepository, never())
                .save(any());

        verify(auditLogService, never())
                .log(any(), anyString(), anyString());
    }

    @Test
    void transferShouldFailWhenSenderAndReceiverAreSame() {

        User receiver = new User();
        receiver.setId(1L);
        receiver.setEmail("sender@example.com");

        when(userRepository.findByEmail("sender@example.com"))
                .thenReturn(Optional.of(receiver));

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(
                        1L,
                        "sender@example.com",
                        new BigDecimal("20.00")
                )
        );

        verify(walletRepository, never())
                .findByUserIdForUpdate(anyLong());

        verify(transactionRepository, never())
                .save(any());

        verify(auditLogService, never())
                .log(any(), anyString(), anyString());
    }

    @Test
    void transferShouldFailWhenReceiverDoesNotExist() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> transferService.transfer(
                        1L,
                        "unknown@example.com",
                        new BigDecimal("20.00")
                )
        );

        verify(walletRepository, never())
                .findByUserIdForUpdate(anyLong());

        verify(transactionRepository, never())
                .save(any());

        verify(auditLogService, never())
                .log(any(), anyString(), anyString());
    }

    @Test
    void transferShouldFailWhenWalletDoesNotExist() {

        User receiver = new User();
        receiver.setId(2L);
        receiver.setEmail("receiver@example.com");

        when(userRepository.findByEmail("receiver@example.com"))
                .thenReturn(Optional.of(receiver));

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> transferService.transfer(
                        1L,
                        "receiver@example.com",
                        new BigDecimal("20.00")
                )
        );

        verify(walletRepository)
                .findByUserIdForUpdate(1L);

        verify(walletRepository, never())
                .findByUserIdForUpdate(2L);

        verify(transactionRepository, never())
                .save(any());

        verify(auditLogService, never())
                .log(any(), anyString(), anyString());
    }

    @Test
    void transferShouldFailWhenReceiverWalletDoesNotExist() {

        User receiver = new User();
        receiver.setId(2L);
        receiver.setEmail("receiver@example.com");

        Wallet senderWallet = new Wallet();
        senderWallet.setUser(user);
        senderWallet.setBalance(new BigDecimal("100.00"));

        when(userRepository.findByEmail("receiver@example.com"))
                .thenReturn(Optional.of(receiver));

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUserIdForUpdate(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> transferService.transfer(
                        1L,
                        "receiver@example.com",
                        new BigDecimal("20.00")
                )
        );

        assertEquals(
                new BigDecimal("100.00"),
                senderWallet.getBalance()
        );

        verify(walletRepository)
                .findByUserIdForUpdate(1L);

        verify(walletRepository)
                .findByUserIdForUpdate(2L);

        verify(transactionRepository, never())
                .save(any());

        verify(auditLogService, never())
                .log(any(), anyString(), anyString());
    }
}
package com.finance.wallet;

import com.finance.wallet.entity.User;
import com.finance.wallet.entity.Wallet;
import com.finance.wallet.repository.TransactionRepository;
import com.finance.wallet.repository.UserRepository;
import com.finance.wallet.repository.WalletRepository;
import com.finance.wallet.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.finance.wallet.exception.ResourceNotFoundException;

class TransferServiceTest {

    private TransferService transferService;

    private UserRepository userRepository;
    private WalletRepository walletRepository;
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {

        userRepository = mock(UserRepository.class);
        walletRepository = mock(WalletRepository.class);
        transactionRepository = mock(TransactionRepository.class);

        transferService = new TransferService(
                userRepository,
                walletRepository,
                transactionRepository
        );
    }

    @Test
    void transferShouldFailWhenBalanceIsInsufficient() {

        // Create receiver
        User receiver = new User();
        receiver.setId(2L);
        receiver.setEmail("receiver@example.com");

        // Mock receiver lookup
        when(userRepository.findByEmail("receiver@example.com"))
                .thenReturn(Optional.of(receiver));

        // Create sender wallet with ₹50
        Wallet senderWallet = new Wallet();
        senderWallet.setBalance(new BigDecimal("50.00"));

        // Create receiver wallet with ₹0
        Wallet receiverWallet = new Wallet();
        receiverWallet.setBalance(new BigDecimal("0.00"));

        // Mock wallet lookups
        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUserIdForUpdate(2L))
                .thenReturn(Optional.of(receiverWallet));

        // Try to transfer ₹80 from a ₹50 wallet
        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(
                        1L,
                        "receiver@example.com",
                        new BigDecimal("80.00")
                )
        );

        // Sender balance must remain ₹50
        assertEquals(
                new BigDecimal("50.00"),
                senderWallet.getBalance()
        );

        // Receiver balance must remain ₹0
        assertEquals(
                new BigDecimal("0.00"),
                receiverWallet.getBalance()
        );

        // No transaction should be created
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transferShouldFailWhenSenderAndReceiverAreSame() {

        // Arrange

        User receiver = new User();
        receiver.setId(1L);
        receiver.setEmail("sender@example.com");

        when(userRepository.findByEmail("sender@example.com"))
                .thenReturn(Optional.of(receiver));

        // Act + Assert

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(
                        1L,
                        "sender@example.com",
                        new BigDecimal("20.00")
                )
        );

        // No wallet should be accessed
        verify(walletRepository, never()).findByUserIdForUpdate(anyLong());

        // No transaction should be created
        verify(transactionRepository, never()).save(any());
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

        verify(walletRepository, never()).findByUserIdForUpdate(anyLong());

        verify(transactionRepository, never()).save(any());
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
    }
}
package com.finance.wallet.service;

import com.finance.wallet.entity.User;
import com.finance.wallet.entity.Wallet;
import com.finance.wallet.repository.UserRepository;
import com.finance.wallet.repository.WalletRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(String email, String password, String fullName) {

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName);

        User savedUser = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(savedUser);

        walletRepository.save(wallet);

        return savedUser;
    }

    public User loginUser(String email, String password) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
        throw new IllegalArgumentException("Invalid email or password");
    }

    return user;
}

    
}
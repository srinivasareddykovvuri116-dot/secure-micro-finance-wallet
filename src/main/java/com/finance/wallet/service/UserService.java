package com.finance.wallet.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.wallet.entity.User;
import com.finance.wallet.entity.Wallet;
import com.finance.wallet.repository.UserRepository;
import com.finance.wallet.repository.WalletRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService) {

        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
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
        auditLogService.log(
                savedUser,
                "USER_REGISTERED",
                "User registered successfully"
        );

        return savedUser;
    }

    public User loginUser(String email, String password) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
        throw new IllegalArgumentException("Invalid email or password");
    }

    if (!user.isActive()) {
        throw new IllegalArgumentException("Account is suspended");
    }

    return user;
}

    
}
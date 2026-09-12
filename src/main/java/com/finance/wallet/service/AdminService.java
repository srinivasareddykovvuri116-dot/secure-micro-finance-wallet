package com.finance.wallet.service;

import com.finance.wallet.entity.User;
import com.finance.wallet.exception.ResourceNotFoundException;
import com.finance.wallet.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User setUserActiveStatus(Long userId, boolean active) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setActive(active);

        return userRepository.save(user);
    }
}
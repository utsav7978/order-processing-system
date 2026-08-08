package com.orderplatform.user.service.impl;

import com.orderplatform.user.dto.UserResponse;
import com.orderplatform.user.entity.User;
import com.orderplatform.user.exception.ResourceNotFoundException;
import com.orderplatform.user.mapper.UserMapper;
import com.orderplatform.user.repository.UserRepository;
import com.orderplatform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + email));
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with id: " + id));
        return userMapper.toUserResponse(user);
    }
}

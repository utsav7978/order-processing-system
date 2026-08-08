package com.orderplatform.user.service;

import com.orderplatform.user.dto.UserResponse;

public interface UserService {

    UserResponse getProfileByEmail(String email);

    UserResponse getUserById(Long id);
}

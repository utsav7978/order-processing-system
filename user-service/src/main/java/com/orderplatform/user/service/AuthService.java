package com.orderplatform.user.service;

import com.orderplatform.user.dto.JwtResponse;
import com.orderplatform.user.dto.LoginRequest;
import com.orderplatform.user.dto.RegisterRequest;
import com.orderplatform.user.dto.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    JwtResponse login(LoginRequest request);
}

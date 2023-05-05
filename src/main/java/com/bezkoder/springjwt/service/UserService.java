package com.bezkoder.springjwt.service;

import com.bezkoder.springjwt.models.User;

/**
 * Created by USER on 5/5/2023.
 */
public interface UserService {
    User registerUser(User user);
    User findByEmail(String email);
    void changePassword(String email, String newPassword);

    User findById(Long userId);

    User updateUser(User user);
}
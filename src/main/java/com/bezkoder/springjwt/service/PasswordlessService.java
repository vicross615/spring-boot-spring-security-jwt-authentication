package com.bezkoder.springjwt.service;

import com.bezkoder.springjwt.models.User;

/**
 * Created by USER on 5/5/2023.
 */
public interface PasswordlessService {
    String generateLoginLink(User user);
    String generateResetLink(User user);
    User verifyLoginLink(String token);
    User verifyResetLink(String token);
}
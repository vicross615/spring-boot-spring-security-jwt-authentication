package com.bezkoder.springjwt.service;

import com.bezkoder.springjwt.models.User;

/**
 * Created by USER on 5/5/2023.
 */
public interface MfaService {
    void generateAndSendMfaCode(User user);
    boolean verifyMfaCode(User user, String code);
}
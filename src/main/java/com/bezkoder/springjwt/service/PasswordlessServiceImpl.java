package com.bezkoder.springjwt.service;

import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.repository.UserRepository;
import com.bezkoder.springjwt.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by USER on 5/5/2023.
 */
@Service
public class PasswordlessServiceImpl implements PasswordlessService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtil;

    @Override
    public String generateLoginLink(User user) {
        return jwtUtil.generateJwtToken("login:" + user.getUsername());
    }

    @Override
    public String generateResetLink(User user) {
        return jwtUtil.generateJwtToken("reset:" + user.getUsername());
    }

    @Override
    public User verifyLoginLink(String token) {
        return verifyToken(token, "login");
    }

    @Override
    public User verifyResetLink(String token) {
        return verifyToken(token, "reset");
    }

    private User verifyToken(String token, String action) {
        try {
            String subject = jwtUtil.getClaimsFromToken(token).getSubject();
            if (subject.startsWith(action + ":")) {
                String username = subject.substring((action + ":").length());
                return  new User();
                        //userRepository.findById(username); // Replace with actual user loading from the database
            }
        } catch (Exception e) {
            // Log the exception
        }
        return null;
    }
}
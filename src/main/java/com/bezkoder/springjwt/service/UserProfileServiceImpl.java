package com.bezkoder.springjwt.service;

import com.bezkoder.springjwt.models.UserProfile;
import com.bezkoder.springjwt.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by USER on 5/5/2023.
 */
@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Override
    public UserProfile createOrUpdateUserProfile(UserProfile userProfile) {
        return userProfileRepository.save(userProfile);
    }

    @Override
    public UserProfile findByUserId(Long userId) {
        return userProfileRepository.findById(userId).orElse(null);
    }
}
package com.bezkoder.springjwt.controllers;

import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.models.UserProfile;
import com.bezkoder.springjwt.service.UserProfileService;
import com.bezkoder.springjwt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Created by USER on 5/5/2023.
 */
@RestController
@RequestMapping("/user-profile")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileService userProfileService;

    @Value("${app.upload.dir:${user.home}}")
    private String uploadDirectory;

    @PostMapping("/{userId}/upload-photo")
    public ResponseEntity<String> uploadProfilePhoto(@PathVariable Long userId,
                                                     @RequestParam("file") MultipartFile file) throws IOException {
        User user = userService.findById(userId);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!file.isEmpty()) {
            try {
                Path copyLocation = Paths.get(uploadDirectory + File.separator + StringUtils.cleanPath(file.getOriginalFilename()));
                Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);

                UserProfile userProfile = user.getUserProfile();
                if (userProfile == null) {
                    userProfile = new UserProfile();
                    userProfile.setUser(user);
                }
                userProfile.setProfilePhotoPath(copyLocation.toString());
                userProfileService.createOrUpdateUserProfile(userProfile);
                return new ResponseEntity<>("Profile photo uploaded successfully", HttpStatus.OK);

            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/{userId}/update")
    public ResponseEntity<UserProfile> updateUserProfile(@PathVariable Long userId,
                                                         @RequestBody UserProfile userProfileData) {
        User user = userService.findById(userId);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UserProfile userProfile = user.getUserProfile();
        if (userProfile == null) {
            userProfile = new UserProfile();
            userProfile.setUser(user);
        }
        userProfile.setFirstName(userProfileData.getFirstName());
        userProfile.setLastName(userProfileData.getLastName());
        userProfile.setGender(userProfileData.getGender());
        userProfile.setAge(userProfileData.getAge());
        userProfile.setDateOfBirth(userProfileData.getDateOfBirth());
        userProfile.setMaritalStatus(userProfileData.getMaritalStatus());
        userProfile.setNationality(userProfileData.getNationality());

        UserProfile updatedUserProfile = userProfileService.createOrUpdateUserProfile(userProfile);
        return new ResponseEntity<>(updatedUserProfile, HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable Long userId) {
        UserProfile userProfile = userProfileService.findByUserId(userId);
        if (userProfile != null) {
            return new ResponseEntity<>(userProfile, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
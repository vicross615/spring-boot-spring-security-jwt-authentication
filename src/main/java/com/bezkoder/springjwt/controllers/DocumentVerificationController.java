package com.bezkoder.springjwt.controllers;

import com.bezkoder.springjwt.models.DocumentVerification;
import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.models.VerificationStatus;
import com.bezkoder.springjwt.service.DocumentVerificationService;
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
@RequestMapping("/verification")
public class DocumentVerificationController {

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentVerificationService documentVerificationService;

    @Value("${app.upload.dir:${user.home}}")
    private String uploadDirectory;

    @PostMapping("/{userId}/submit")
    public ResponseEntity<String> submitDocument(@PathVariable Long userId,
                                                 @RequestParam("documentId") String documentId,
                                                 @RequestParam("file") MultipartFile file) {
        User user = userService.findById(userId);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!file.isEmpty()) {
            try {
                Path copyLocation = Paths.get(uploadDirectory + File.separator + StringUtils.cleanPath(file.getOriginalFilename()));
                Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);

                DocumentVerification documentVerification = new DocumentVerification();
                documentVerification.setUser(user);
                documentVerification.setDocumentId(documentId);
                documentVerification.setDocumentImagePath(copyLocation.toString());

                documentVerificationService.submitVerification(documentVerification);
                return new ResponseEntity<>("Document submitted successfully", HttpStatus.OK);

            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/{userId}/callback")
    public ResponseEntity<String> documentVerificationCallback(@PathVariable Long userId,
                                                               @RequestParam("status") VerificationStatus status) {
        DocumentVerification documentVerification = documentVerificationService.updateVerificationStatus(userId, status);
        if (documentVerification != null) {
            // Optionally, send a notification to the user when the account is verified
            if (status == VerificationStatus.VERIFIED) {
                // Send a notification to the user
            }
            return new ResponseEntity<>("Verification status updated successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


}
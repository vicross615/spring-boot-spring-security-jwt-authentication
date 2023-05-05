package com.bezkoder.springjwt.service;

import com.bezkoder.springjwt.models.DocumentVerification;
import com.bezkoder.springjwt.models.VerificationStatus;

/**
 * Created by USER on 5/5/2023.
 */
public interface DocumentVerificationService {
    DocumentVerification submitVerification(DocumentVerification documentVerification);
    DocumentVerification findByUserId(Long userId);
    DocumentVerification updateVerificationStatus(Long userId, VerificationStatus status);
}
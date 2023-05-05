package com.bezkoder.springjwt.repository;

import com.bezkoder.springjwt.models.DocumentVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by USER on 5/5/2023.
 */
@Repository
public interface DocumentVerificationRepository extends JpaRepository<DocumentVerification, Long> {
    Optional<DocumentVerification> findByUserId(Long userId);
}
package com.bezkoder.springjwt.repository;

/**
 * Created by USER on 5/5/2023.
 */
import com.bezkoder.springjwt.models.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
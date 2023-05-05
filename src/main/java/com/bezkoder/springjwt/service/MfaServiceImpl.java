package com.bezkoder.springjwt.service;

import com.bezkoder.springjwt.models.User;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by USER on 5/5/2023.
 */
@Service
public class MfaServiceImpl implements MfaService {

    private static final int MFA_CODE_LENGTH = 6;
    private static final int MFA_CODE_EXPIRATION_MINUTES = 5;
    private static final Map<String, MfaCode> mfaCodes = new HashMap<>();

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void generateAndSendMfaCode(User user) {
        String code = generateMfaCode();
        mfaCodes.put(user.getUsername(), new MfaCode(code, System.currentTimeMillis()));
        // Send the code to the user, e.g., via SMS or email
    }

    @Override
    public boolean verifyMfaCode(User user, String code) {
        MfaCode storedCode = mfaCodes.get(user.getUsername());
        if (storedCode != null && storedCode.getCode().equals(code)) {
            long currentTimeMillis = System.currentTimeMillis();
            long codeAgeMillis = currentTimeMillis - storedCode.getCreationTime();
            if (TimeUnit.MILLISECONDS.toMinutes(codeAgeMillis) <= MFA_CODE_EXPIRATION_MINUTES) {
                mfaCodes.remove(user.getUsername());
                return true;
            }
        }
        return false;
    }

    private String generateMfaCode() {
        StringBuilder codeBuilder = new StringBuilder(MFA_CODE_LENGTH);
        for (int i = 0; i < MFA_CODE_LENGTH; i++) {
            codeBuilder.append(secureRandom.nextInt(10));
        }
        return codeBuilder.toString();
    }

    private static class MfaCode {
        private final String code;
        private final long creationTime;

        public MfaCode(String code, long creationTime) {
            this.code = code;
            this.creationTime = creationTime;
        }

        public String getCode() {
            return code;
        }

        public long getCreationTime() {
            return creationTime;
        }
    }
}
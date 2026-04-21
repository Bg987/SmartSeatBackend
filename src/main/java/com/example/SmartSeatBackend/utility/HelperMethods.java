package com.example.SmartSeatBackend.utility;

import com.example.SmartSeatBackend.entity.College;
import com.example.SmartSeatBackend.repository.CollegeRepository;
import com.example.SmartSeatBackend.repository.StudentRepository;
import com.example.SmartSeatBackend.service.MessageService;
import com.example.SmartSeatBackend.service.UniversityService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;

@Component
@AllArgsConstructor
public class HelperMethods {

    private final CollegeRepository collegeRepo;
    private final StudentRepository stuRepo;
    private final MessageService msgService;

    private static final String ALGORITHM = "AES";
    // This long string will now be hashed to exactly 32 bytes (256 bits)
    private static final String AES_KEY_SEED = "my_super_secret_kdsvnjdnigdgudgyubduhygbyugdubguhdgdbuhgbudgbudbgudbgudbgggbuegububgyeubgebutebuudbgbdgbdbghdibgvinhgvuirnvyurnyunhtrnhyurnhyur";

    /**
     * Internal helper to generate a valid 32-byte AES key from the long seed string.
     */
    private SecretKeySpec getSecretKey() throws Exception {
        byte[] key = AES_KEY_SEED.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key); // This results in exactly 32 bytes
        return new SecretKeySpec(key, ALGORITHM);
    }

    public Long getCollegeIdByUserId() throws Exception {
        Long userId = Long.valueOf(getId());
        return collegeRepo.findByUser_userId(userId)
                .map(College::getCollegeId)
                .orElseThrow(() -> new RuntimeException("College not found for User ID: " + userId));
    }

    public String getEnrNumberIdByUserId() throws Exception {
        Long studentId = Long.valueOf(getId());
        return stuRepo.findEnrollmentNoByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Enrollment number not found for student ID: " + studentId));
    }

    public String getId() throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("No authentication found in security context");
        }

        // The principal is usually the "subject" from your JWT (the encrypted ID string)
        String principal = auth.getPrincipal().toString();
        return String.valueOf(decrypt(principal));
    }

    public String getRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        return auth.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
    }

    public String encrypt(Long id) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
        byte[] encryptedBytes = cipher.doFinal(String.valueOf(id).getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public Long decrypt(String encryptedId) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedId);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return Long.parseLong(new String(decryptedBytes));
    }

    public void sendRegistrationBatch(List<UniversityService.RegistrationDetail> details) {
        for (UniversityService.RegistrationDetail detail : details) {
            try {
                msgService.sendRegistrationEvent(
                        detail.email(),
                        detail.password(),
                        detail.name(),
                        detail.collegeID()
                );
            } catch (Exception e) {
                System.err.println("Failed to send email for: " + detail.email() + " Error: " + e.getMessage());
            }
        }
    }
}
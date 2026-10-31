package com.stockpilot.backend.auth;

import com.stockpilot.backend.auth.dto.PasswordResetMessage;
import com.stockpilot.backend.user.AppUser;
import com.stockpilot.backend.user.AppUserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String GENERIC_MESSAGE = "If an account exists for that email, reset instructions will be sent.";
    private final AppUserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final long expirationMinutes;
    private final String frontendUrl;
    private final String fromAddress;

    public PasswordResetService(AppUserRepository users, PasswordResetTokenRepository tokens,
                                org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                                ObjectProvider<JavaMailSender> mailSenders,
                                @Value("${app.mail.enabled:false}") boolean mailEnabled,
                                @Value("${app.password-reset.expiration-minutes:15}") long expirationMinutes,
                                @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl,
                                @Value("${app.mail.from:}") String fromAddress) {
        this.users = users;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSenders.getIfAvailable();
        this.mailEnabled = mailEnabled;
        this.expirationMinutes = expirationMinutes;
        this.frontendUrl = frontendUrl;
        this.fromAddress = fromAddress;
    }

    @Transactional
    public PasswordResetMessage requestReset(String email) {
        if (!mailEnabled || mailSender == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Password reset email is not configured yet. Please contact your administrator.");
        }
        Optional<AppUser> optionalUser = users.findByEmailIgnoreCase(email.trim());
        if (optionalUser.isEmpty()) return new PasswordResetMessage(GENERIC_MESSAGE);

        AppUser user = optionalUser.get();
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String tokenHash = sha256(rawToken);
        tokens.deleteAllByUser_Id(user.getId());
        tokens.save(new PasswordResetToken(user, tokenHash, Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES)));

        String resetUrl = frontendUrl.replaceAll("/$", "") + "/?resetToken=" + rawToken;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            if (!fromAddress.isBlank()) message.setFrom(fromAddress);
            message.setSubject("Reset your StockPilot password");
            message.setText("Hello " + user.getFullName() + ",\n\nUse this link to choose a new password. It expires in "
                    + expirationMinutes + " minutes and can only be used once:\n\n" + resetUrl
                    + "\n\nIf you did not request this, you can ignore this email.");
            mailSender.send(message);
        } catch (RuntimeException ex) {
            tokens.deleteAllByUser_Id(user.getId());
            log.warn("Password reset email delivery failed.");
        }
        return new PasswordResetMessage(GENERIC_MESSAGE);
    }

    @Transactional
    public PasswordResetMessage resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokens.findByTokenHash(sha256(rawToken))
                .filter(candidate -> candidate.isActiveAt(Instant.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "This reset link is invalid or expired. Request a new one."));
        AppUser user = token.getUser();
        user.updatePasswordHash(passwordEncoder.encode(newPassword));
        token.markUsed(Instant.now());
        tokens.deleteAllByUser_Id(user.getId());
        return new PasswordResetMessage("Your password has been reset. You can now sign in.");
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable.", ex);
        }
    }
}

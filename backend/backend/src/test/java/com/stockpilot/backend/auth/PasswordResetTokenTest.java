package com.stockpilot.backend.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class PasswordResetTokenTest {
    @Test
    void tokenExpiresAtItsDeadline() {
        Instant now = Instant.parse("2026-10-31T10:00:00Z");
        PasswordResetToken token = new PasswordResetToken(null, "hashed-token", now.plusSeconds(60));

        assertTrue(token.isActiveAt(now));
        assertFalse(token.isActiveAt(now.plusSeconds(60)));
    }

    @Test
    void usedTokenCannotBeReused() {
        Instant now = Instant.parse("2026-10-31T10:00:00Z");
        PasswordResetToken token = new PasswordResetToken(null, "hashed-token", now.plusSeconds(60));

        token.markUsed(now);

        assertFalse(token.isActiveAt(now.plusSeconds(1)));
    }
}

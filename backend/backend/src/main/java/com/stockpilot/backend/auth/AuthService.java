package com.stockpilot.backend.auth;

import com.stockpilot.backend.auth.dto.AuthResponse;
import com.stockpilot.backend.auth.dto.LoginRequest;
import com.stockpilot.backend.auth.dto.RegisterRequest;
import com.stockpilot.backend.business.Business;
import com.stockpilot.backend.business.BusinessRepository;
import com.stockpilot.backend.user.AppUser;
import com.stockpilot.backend.user.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final AppUserRepository users;
    private final BusinessRepository businesses;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long expirationMillis;

    public AuthService(AppUserRepository users, BusinessRepository businesses,
                       PasswordEncoder passwordEncoder, JwtService jwtService,
                       @Value("${app.jwt.expiration-ms:3600000}") long expirationMillis) {
        this.users = users;
        this.businesses = businesses;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.expirationMillis = expirationMillis;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) throw new EmailAlreadyRegisteredException();

        Business business = businesses.save(new Business(request.businessName().trim()));
        AppUser user = users.save(new AppUser(
                request.fullName().trim(), email, passwordEncoder.encode(request.password()), Role.OWNER, business));
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }
        return response(user);
    }

    public AuthResponse response(AppUser user) {
        return new AuthResponse(jwtService.createToken(user), "Bearer", expirationMillis / 1000,
                user.getId(), user.getFullName(), user.getEmail(), user.getRole(),
                user.getBusiness().getId(), user.getBusiness().getName());
    }
}

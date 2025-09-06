package com.example.bankcards.service;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public JwtResponse register(AuthRequest request) {
        try {
            System.out.println("=== REGISTRATION DEBUG ===");
            System.out.println("Checking username: " + request.getUsername());

            // Детальная проверка существования пользователя
            Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
            System.out.println("User exists in DB: " + existingUser.isPresent());

            if (existingUser.isPresent()) {
                System.out.println("Found existing user: " + existingUser.get().getUsername());
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Username '" + request.getUsername() + "' already exists"
                );
            }

            System.out.println("Creating new user...");

            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getUsername() + "@example.com");
            user.setRole(Role.ROLE_USER);

            User savedUser = userRepository.save(user);
            System.out.println("User successfully created with ID: " + savedUser.getId());

            // Генерируем реальный JWT токен
            String token = jwtService.generateToken(savedUser);

            return new JwtResponse(token, "Bearer", savedUser.getUsername(), savedUser.getRole().name());

        } catch (ResponseStatusException e) {
            throw e; // Пробрасываем уже обработанные ошибки
        } catch (Exception e) {
            System.err.println("Registration failed with error: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Registration failed: " + e.getMessage()
            );
        }
    }

    public JwtResponse authenticate(AuthRequest request) {
        try {
            System.out.println("=== LOGIN DEBUG ===");
            System.out.println("Attempting login for: " + request.getUsername());

            // Сначала проверяем существование пользователя
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> {
                        System.out.println("User not found: " + request.getUsername());
                        return new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid username or password"
                        );
                    });

            System.out.println("User found, attempting authentication...");

            // Аутентификация
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            System.out.println("Authentication successful, generating token...");

            String token = jwtService.generateToken(user);

            return new JwtResponse(token, "Bearer", user.getUsername(), user.getRole().name());

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication failed: " + e.getMessage()
            );
        }
    }

    // Вспомогательный метод для проверки
    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}
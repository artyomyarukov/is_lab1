package com.yarukov.service;

import com.yarukov.dao.UserDAO;
import com.yarukov.dto.UserDTO;
import com.yarukov.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserDAO userDAO;
    private final JwtService jwtService;

    @Transactional
    public Map<String, String> register(UserDTO dto) {
        if (userDAO.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalStateException("Пользователь с именем '" + dto.getUsername() + "' уже существует!");
        }
        User user = User.builder()
                .username(dto.getUsername())
                .passwordHash(hashPassword(dto.getPassword()))
                .build();
        userDAO.save(user);

        String token = jwtService.generateToken(user.getUsername());
        return Map.of("token", token, "username", user.getUsername());
    }

    public Map<String, String> login(UserDTO dto) {
        User user = userDAO.findByUsername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Неверный логин"));

        if (!user.getPasswordHash().equals(hashPassword(dto.getPassword()))) {
            throw new IllegalArgumentException("Неверный пароль");
        }

        String token = jwtService.generateToken(user.getUsername());
        return Map.of("token", token, "username", user.getUsername());
    }

    private String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
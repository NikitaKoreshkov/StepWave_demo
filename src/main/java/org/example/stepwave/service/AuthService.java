package org.example.stepwave.service;

import org.example.stepwave.model.User;
import org.example.stepwave.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisCacheService redisCacheService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean authenticate(String username, String password) {
        // Шаг 1: Проверяем кэш
        String cachedHash = redisCacheService.getCachedPassword(username);
        if (cachedHash != null) {
            return passwordEncoder.matches(password, cachedHash);
        }

        // Шаг 2: Если в кэше нет, ищем в базе
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String dbHash = user.getPassword();

            // Сохраняем в кэш
            redisCacheService.cachePassword(username, dbHash);

            // Проверяем пароль
            return passwordEncoder.matches(password, dbHash);
        }

        return false; // Пользователь не найден
    }
}
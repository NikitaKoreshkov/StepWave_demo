package org.example.stepwave.controller;

import org.example.stepwave.model.User;
import org.example.stepwave.repository.UserRepository;
import org.example.stepwave.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;
@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    // Создаем BCryptPasswordEncoder прямо в контроллере
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Метод для отправки письма
    @PostMapping("/send")
    public String sendEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String text) {
        emailService.sendHtmlEmail(to, subject, text); // Используем sendHtmlEmail вместо sendEmail
        return "Email отправлен на " + to;
    }

    // Метод для запроса сброса пароля (с использованием username вместо email)
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody UsernameRequest usernameRequest) {
        String username = usernameRequest.getUsername();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Генерация токена сброса пароля
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(java.time.LocalDateTime.now().plusHours(24)); // Токен истекает через 24 часа
            userRepository.save(user);

            String resetUrl = "http://localhost:8082/api/email/reset-password?token=" + token;

            // Отправка письма с ссылкой на сброс пароля
            emailService.sendPasswordResetEmail(user.getUsername(), resetUrl); // Используем метод sendPasswordResetEmail

            return ResponseEntity.ok("Если такой пользователь существует, ссылка для сброса пароля отправлена на указанный username.");
        }

        return ResponseEntity.status(400).body("Пользователь с таким username не найден.");
    }

    // Метод для сброса пароля (при переходе по ссылке)
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam("token") String token,
                                                @RequestParam("newPassword") String newPassword) {
        Optional<User> userOpt = userRepository.findByResetToken(token);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Проверка срока действия токена
            if (user.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
                return ResponseEntity.status(400).body("Срок действия токена истек.");
            }

            // Обновление пароля
            user.setPassword(passwordEncoder.encode(newPassword)); // Хешируем новый пароль с помощью BCrypt
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);

            return ResponseEntity.ok("Пароль успешно обновлен.");
        }

        return ResponseEntity.status(400).body("Неверный или истекший токен.");
    }
}

class UsernameRequest {
    private String username;

    // Геттеры и сеттеры
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

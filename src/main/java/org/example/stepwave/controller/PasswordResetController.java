package org.example.stepwave.controller;

import org.example.stepwave.model.User;
import org.example.stepwave.repository.UserRepository;
import org.example.stepwave.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.util.Optional;

@Controller
public class PasswordResetController {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Метод для отображения страницы сброса пароля
    @GetMapping("/reset-password-form")
    public String showPasswordResetPage(@RequestParam("token") String token, Model model) {
        Optional<User> userOpt = userRepository.findByResetToken(token);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Проверка срока действия токена
            if (user.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
                model.addAttribute("error", "Срок действия токена истек.");
                return "reset-password-form";
            }
            model.addAttribute("token", token);
            return "reset-password-form"; // Отображаем страницу сброса пароля
        }

        model.addAttribute("error", "Неверный или истекший токен.");
        return "reset-password-form"; // Если токен неверный или истек
    }

    // Метод для сброса пароля через API (обработать POST запрос)
    @PostMapping("/api/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        // Логируем полученные данные для диагностики
        System.out.println("Received token: " + request.getToken());
        System.out.println("Received newPassword: " + request.getNewPassword());

        // Поиск пользователя по токену
        Optional<User> userOpt = userRepository.findByResetToken(request.getToken());

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Проверка срока действия токена
            if (user.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
                return ResponseEntity.status(400).body("Срок действия токена истек.");
            }

            // Обновление пароля
            user.setPassword(passwordEncoder.encode(request.getNewPassword())); // Хешируем новый пароль
            user.setResetToken(null); // Убираем токен
            user.setResetTokenExpiry(null); // Убираем срок действия токена
            userRepository.save(user); // Сохраняем обновленного пользователя

            return ResponseEntity.ok("Пароль успешно обновлен.");
        }

        return ResponseEntity.status(400).body("Неверный или истекший токен.");
    }

    // DTO класс для сброса пароля
    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;

        // Геттеры и сеттеры
        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
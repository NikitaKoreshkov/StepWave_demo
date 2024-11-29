package org.example.stepwave.controller;

import org.example.stepwave.model.User;
import org.example.stepwave.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Получение всех пользователей
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Создание нового пользователя
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty() ||
                user.getPassword() == null || user.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Имя пользователя и пароль не могут быть пустыми.\"}");
        }

        // Проверка пароля
        if (user.getPassword().length() < 8 || !user.getPassword().matches(".*[A-Z].*")) {
            return ResponseEntity.badRequest().body("{\"error\": \"Пароль должен быть не менее 8 символов и содержать хотя бы одну заглавную букву.\"}");
        }

        if (userService.isUsernameTaken(user.getUsername())) {
            return ResponseEntity.badRequest().body("{\"error\": \"Извините, данное имя пользователя уже занято.\"}");
        }

        // Хэшируем пароль
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        userService.createUser(user);
        return ResponseEntity.ok("{\"message\": \"Вы успешно создали аккаунт, пожалуйста войдите в систему.\"}");
    }

    // Проверка, занято ли имя пользователя
    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsername(@PathVariable String username) {
        if (userService.isUsernameTaken(username)) {
            return ResponseEntity.ok("{\"exists\": true}");  // Если имя занято
        } else {
            return ResponseEntity.ok("{\"exists\": false}");  // Если имя свободно
        }
    }

    // Получение пользователя по ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return ResponseEntity.ok(user);
    }

    // Обновление пользователя
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        // Проверка пароля
        if (updatedUser.getPassword().length() < 8 || !updatedUser.getPassword().matches(".*[A-Z].*")) {
            return ResponseEntity.badRequest().body("{\"error\": \"Пароль должен быть не менее 8 символов и содержать хотя бы одну заглавную букву.\"}");
        }

        // Хэшируем новый пароль
        String hashedPassword = passwordEncoder.encode(updatedUser.getPassword());
        updatedUser.setPassword(hashedPassword);

        User updated = userService.updateUser(id, updatedUser);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.status(404).body("{\"error\": \"Пользователь не найден.\"}");
        }
    }

    // Удаление пользователя
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(404).body("{\"error\": \"Пользователь не найден.\"}");
        }
    }
}

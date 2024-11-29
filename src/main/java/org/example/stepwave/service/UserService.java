package org.example.stepwave.service;

import org.example.stepwave.model.User;
import org.example.stepwave.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Получить всех пользователей
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Проверить, занято ли имя пользователя
    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    // Получить пользователя по ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Создать нового пользователя
    public void createUser(User user) {
        // Хэшируем пароль перед сохранением
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }

    // Обновить данные пользователя
    public User updateUser(Long id, User updatedUser) {
        if (userRepository.existsById(id)) {
            updatedUser.setId(id);
            // Хэшируем новый пароль перед обновлением
            String hashedPassword = passwordEncoder.encode(updatedUser.getPassword());
            updatedUser.setPassword(hashedPassword);
            return userRepository.save(updatedUser);
        } else {
            return null;
        }
    }

    // Удалить пользователя по ID
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}

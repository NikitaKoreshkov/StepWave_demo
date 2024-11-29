package org.example.stepwave.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import javax.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail; // Email отправителя из application.properties

    @Value("${app.base-url}")
    private String baseUrl; // Базовый URL из настроек, например, "http://localhost:8082"

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Метод для отправки HTML-письма
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // true для поддержки HTML
            helper.setFrom(fromEmail);  // Устанавливаем отправителя
            helper.setTo(to);           // Кому отправляем
            helper.setSubject(subject); // Тема письма
            helper.setText(htmlContent, true); // true для того, чтобы отправить HTML

            mailSender.send(message); // Отправляем письмо
            System.out.println("HTML письмо отправлено успешно!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Метод для формирования ссылки сброса пароля
    public String generatePasswordResetLink(String token) {
        return baseUrl + "/reset-password-form?token=" + token;
    }

    // Метод для отправки письма с ссылкой на сброс пароля (HTML)
    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Сброс пароля";

        // Формируем ссылку для сброса пароля
        String resetUrl = generatePasswordResetLink(token);

        // HTML-содержание письма с инлайновыми стилями
        String htmlContent = "<!DOCTYPE html>" +
                "<html lang=\"ru\">" +
                "<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>Сброс пароля</title></head>" +
                "<body>" +
                "<div style=\"text-align: center; font-family: Arial, sans-serif; color: #333;\">" +
                "<h1 style=\"color: #007bff;\">Сброс пароля для вашего аккаунта в StepWave</h1>" +
                "<p>Если это не вы, проигнорируйте это письмо.</p>" +
                "<p>Вы запросили сброс пароля для вашего аккаунта в компании StepWave. Чтобы продолжить, перейдите по следующей ссылке:</p>" +
                "<a href=\"" + resetUrl + "\" style=\"display: inline-block; padding: 12px 25px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px; font-weight: bold;\">Сбросить пароль</a>" +
                "<p>Ссылка будет действительна в течение 24 часов.</p>" +
                "<p>С уважением,<br>Команда StepWave</p>" +
                "</div>" +
                "</body>" +
                "</html>";

        // Отправка HTML-письма
        sendHtmlEmail(to, subject, htmlContent);
    }
}
package com.smart_extract_service.modules.auth.infrastructure.adapters.external;

import com.smart_extract_service.modules.auth.domain.ports.output.EmailOutputPort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class MailPersistenceAdapter implements EmailOutputPort {

    private final JavaMailSender mailSender;

    public MailPersistenceAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@movimenttech.com.br");
        message.setTo(to);
        message.setSubject("Recuperação de Senha - Moviment Tech");
        message.setText("Para redefinir sua senha, utilize o token a seguir: " + token +
                "\nEste token expira em 15 minutos.");
        mailSender.send(message);

    }
}

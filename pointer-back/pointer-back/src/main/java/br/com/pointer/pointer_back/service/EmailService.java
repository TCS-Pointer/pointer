package br.com.pointer.pointer_back.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    public void sendPasswordEmail(String recipientEmail, String password, String name) {
        String templateId = "d-d833e5b4a1e84774b566a29a2bd2e984";
        Map<String, String> dynamicData = Map.of("senha", password, "nome", name);

        Mail mail = createTemplateMail(recipientEmail, templateId, dynamicData);

        sendMail(mail);
    }

    public void sendVerificationCodeEmail(String recipientEmail, String name) {
        String templateId = "d-8b3d5327466b48239ddc170d54af8bce";
        String code = generateRandomCode();
        Map<String, String> dynamicData = Map.of("codigo", code, "nome", name);
        Mail mail = createTemplateMail(recipientEmail, templateId, dynamicData);
        verificationCodes.put(recipientEmail, code);
        sendMail(mail);
    }

    public boolean verifyCode(String email, String code) {
        return code.equals(verificationCodes.get(email));
    }

    public void removeVerificationCode(String email) {
        verificationCodes.remove(email);
    }

    private Mail createTemplateMail(String recipientEmail, String templateId, Map<String, String> dynamicData) {
        Email from = new Email("no-replypointer@bol.com.br");
        Email to = new Email(recipientEmail);
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setTemplateId(templateId);

        Personalization personalization = new Personalization();
        personalization.addTo(to);
        dynamicData.forEach(personalization::addDynamicTemplateData);

        mail.addPersonalization(personalization);
        return mail;
    }

    private void sendMail(Mail mail) {
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException ex) {
            throw new EmailSendingException("Failed to send email", ex);
        }
    }

    private String generateRandomCode() {
        Random random = new Random();
        int code = 100_000 + random.nextInt(900_000);
        return String.valueOf(code);
    }

    // Exceção customizada
    private static class EmailSendingException extends RuntimeException {
        public EmailSendingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

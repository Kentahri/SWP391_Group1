package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.entity.OtpMail;
import com.group1.swp.pizzario_swp391.repository.OtpMailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class OtpMailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private OtpMailRepository otpMailRepository;

    public void sendEmail(String to, String subject, String content){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }

    public void addOtpMail(OtpMail otpMail){
        otpMailRepository.save(otpMail);
    }

}

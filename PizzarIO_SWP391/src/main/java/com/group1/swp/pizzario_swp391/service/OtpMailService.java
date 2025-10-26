package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.entity.OtpMail;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.repository.OtpMailRepository;
import com.group1.swp.pizzario_swp391.repository.StaffRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpMailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private OtpMailRepository otpMailRepository;

    @Autowired
    private StaffRepository staffRepo;

    public void sendEmail(String to, String subject, String content){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }

    @Transactional
    public OtpMail createNewOtp(int staffId) {
        otpMailRepository.markAllUsedByStaff(staffId);

        Staff staff = staffRepo.findById(staffId).orElseThrow();

        String code = String.format("%06d", new Random().nextInt(1_000_000));

        OtpMail otp = new OtpMail(code, LocalDateTime.now(), LocalDateTime.now().plusMinutes(3));

        staff.addOtpMail(otp);
        staffRepo.save(staff);

        return otp;

    }

    public OtpMail getLatestOtpByStaffId(int staffId) {
        // gọi repository và truyền staffId vào
        return otpMailRepository.findLatestOtpByStaffId(staffId);
    }

    public boolean checkExpireOtp(OtpMail otp){
        LocalDateTime now = LocalDateTime.now();
        boolean check = false;

        if(otp.getExpiresAt().isBefore(now)){
            check = true;
        }

        return check;
    }

    public boolean checkUsedOtp(OtpMail otp){
        return otp.isUsed();
    }

    public void updateOtp(OtpMail otp){
        otpMailRepository.save(otp);
    }
}

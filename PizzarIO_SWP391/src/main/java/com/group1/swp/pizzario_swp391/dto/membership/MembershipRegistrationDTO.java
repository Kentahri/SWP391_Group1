package com.group1.swp.pizzario_swp391.dto.membership;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MembershipRegistrationDTO {
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 200, message = "Họ và tên quá dài")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 50, message = "Số điện thoại quá dài")
    private String phoneNumber;

    public MembershipRegistrationDTO() {}

    public MembershipRegistrationDTO(String fullName, String phoneNumber) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
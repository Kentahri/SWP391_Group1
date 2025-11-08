package com.group1.swp.pizzario_swp391.dto.membership;

import jakarta.validation.constraints.NotBlank;

public class VerifyMembershipDTO {
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    public VerifyMembershipDTO() {}

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}

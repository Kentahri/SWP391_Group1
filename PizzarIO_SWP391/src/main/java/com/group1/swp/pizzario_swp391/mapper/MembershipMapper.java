package com.group1.swp.pizzario_swp391.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.group1.swp.pizzario_swp391.dto.membership.MembershipDTO;
import com.group1.swp.pizzario_swp391.dto.membership.VerifyMembershipDTO;
import com.group1.swp.pizzario_swp391.dto.membership.MembershipRegistrationDTO;
import com.group1.swp.pizzario_swp391.entity.Membership;

@Mapper(componentModel = "spring")
public interface MembershipMapper {
    // Base mapper (entity <-> basic DTO)
    Membership toMembership(MembershipDTO membershipDTO);
    void updateMembership(@MappingTarget Membership membership, MembershipDTO membershipDTO);
    MembershipDTO toMembershipDTO(Membership membership);
    List<MembershipDTO> toMembershipDTOs(List<Membership> memberships);

    // Mapper for verify flow (form DTO)
    Membership toMembership(VerifyMembershipDTO verifyMembershipDTO);
    void updateMembershipFromVerify(@MappingTarget Membership membership, VerifyMembershipDTO verifyMembershipDTO);

    // Mapper for registration flow
    Membership toMembership(MembershipRegistrationDTO registrationDTO);
    void updateMembershipFromRegistration(@MappingTarget Membership membership, MembershipRegistrationDTO registrationDTO);
}
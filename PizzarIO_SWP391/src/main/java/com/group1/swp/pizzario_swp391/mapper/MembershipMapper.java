package com.group1.swp.pizzario_swp391.mapper;

import org.mapstruct.Mapper;

import com.group1.swp.pizzario_swp391.dto.membership.MembershipDTO;
import com.group1.swp.pizzario_swp391.dto.membership.MembershipRegistrationDTO;
import com.group1.swp.pizzario_swp391.entity.Membership;

@Mapper(componentModel = "spring")
public interface MembershipMapper {
    MembershipDTO toMembershipDTO(Membership membership);

    @org.mapstruct.Mapping(source = "fullName", target = "name")
    Membership toMembership(MembershipRegistrationDTO registrationDTO);
}
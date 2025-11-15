package com.group1.swp.pizzario_swp391.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Configuration
@ConfigurationProperties(prefix = "pizzario.settings")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Setting {
    int conflictReservationMinutes;
    int autoLockReservationMinutes;
    int noShowWaitMinutes;
    int noAbsentCheckMinutes;
    int noCheckoutCheckMinutes;
    int reLoginTimeoutMinutes;
}

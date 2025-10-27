package com.group1.swp.pizzario_swp391.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pizzario.settings")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Setting {
    int conflictReservationMinutes;
    int autoLockReservationMinutes;
    int noShowWaitMinutes;
}

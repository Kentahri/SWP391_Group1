package com.group1.swp.pizzario_swp391.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler(); // ThreadPool này implement TaskScheduler, tạo ra nhiều thread, giúp hệ thống có thể chạy song song cùng 1 lúc nhiều cái schedule
        scheduler.setPoolSize(5); //Tối đa 5
        scheduler.setThreadNamePrefix("reservation-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true); // Cấu hình hành vi khi shutdown server - nghĩa là n sẽ hoàn thành nốt task r mới shutdown
        scheduler.setAwaitTerminationSeconds(60); // Thời gian tối đa chờ task đấy n hoàn thành
        scheduler.initialize();
        return scheduler;

    }

    // ✅ THÊM MỚI: TaskScheduler riêng cho Staff Scheduling
    @Bean(name = "staffTaskScheduler")
    public TaskScheduler staffTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10); // ✅ Tăng lên 10 threads cho staff scheduling
        scheduler.setThreadNamePrefix("staff-scheduler-"); // ✅ Prefix riêng để debug dễ hơn
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.initialize();
        return scheduler;
    }
}

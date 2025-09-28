package com.group1.swp.pizzario_swp391;


import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.service.StaffService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PizzarIoSwp391Application {

    public static void main(String[] args) {
        SpringApplication.run(PizzarIoSwp391Application.class, args);
    }

    @Bean
    CommandLineRunner initData(StaffService service) {
        return (String[] args) -> {

            Staff s1 = new Staff(
                    "Nguyễn Văn A",
                    java.time.LocalDate.of(1995, 5, 10),
                    "0901234567",
                    "Hà Nội",
                    "123456",
                    "hhcc782005@gmail.com",
                    Staff.Role.CASHIER,
                    true
            );

            Staff s2 = new Staff(
                    "Trần Thị B",
                    java.time.LocalDate.of(1992, 8, 20),
                    "0907654321",
                    "TP.HCM",
                    "123456",
                    "huy@gmail.com",
                    Staff.Role.MANAGER,
                    true
            );

            Staff s3 = new Staff(
                    "Lê Hữu C",
                    java.time.LocalDate.of(1998, 1, 15),
                    "0912345678",
                    "Đà Nẵng",
                    "123456",
                    "hiep@gmail.com",
                    Staff.Role.KITCHEN,
                    true
            );

            service.add(s1);
            service.add(s2);
            service.add(s3);
        };
    }
}

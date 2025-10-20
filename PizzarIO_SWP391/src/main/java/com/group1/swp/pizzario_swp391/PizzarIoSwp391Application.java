package com.group1.swp.pizzario_swp391;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

import com.group1.swp.pizzario_swp391.entity.DiningTable;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.service.StaffService;
import com.group1.swp.pizzario_swp391.service.TableService;


@SpringBootApplication
public class PizzarIoSwp391Application {

    public static void main(String[] args) {
        SpringApplication.run(PizzarIoSwp391Application.class, args);
    }

    @Bean
    CommandLineRunner initData(StaffService service, TableService tableService) {
        return (String[] args) -> {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            // Create sample staff
            Staff s1 = new Staff(
                    "Nguyen Van A",
                    java.time.LocalDate.of(1995, 5, 10),
                    "0901234567",
                    "Hà Nội",
                    passwordEncoder.encode("123456"),
                    "tranquochaibavi@gmail.com",
                    Staff.Role.CASHIER,
                    true
            );

            Staff s2 = new Staff(
                    "Tran Thi B",
                    java.time.LocalDate.of(1992, 8, 20),
                    "0907654321",
                    "TP.HCM",
                    "123456",
                    "huy@gmail.com",
                    Staff.Role.MANAGER,
                    true
            );

            Staff s3 = new Staff(
                    "Le Huu C",
                    java.time.LocalDate.of(1998, 1, 15),
                    "0912345678",
                    "Đà Nẵng",
                    passwordEncoder.encode("123456"),
                    "hiep@gmail.com",
                    Staff.Role.KITCHEN,
                    true
            );

            service.add(s1);
            service.add(s2);
            service.add(s3);

            // Create 10 sample tables
            java.time.LocalDateTime now = java.time.LocalDateTime.now();

            // Bàn 1 - AVAILABLE (Bàn trống) - 4 chỗ
            DiningTable table1 = new DiningTable(
                DiningTable.TableStatus.AVAILABLE,
                DiningTable.TableCondition.GOOD,
                now, now, 4, 0
            );

            // Bàn 2 - AVAILABLE (Bàn trống) - 4 chỗ
            DiningTable table2 = new DiningTable(
                DiningTable.TableStatus.AVAILABLE,
                DiningTable.TableCondition.GOOD,
                now, now, 4, 0
            );

            // Bàn 3 - WAITING_PAYMENT (Chờ thanh toán) - 2 chỗ
            DiningTable table3 = new DiningTable(
                DiningTable.TableStatus.WAITING_PAYMENT,
                DiningTable.TableCondition.GOOD,
                now, now, 2, 0
            );

            // Bàn 4 - AVAILABLE (Đã thanh toán xong) - 6 chỗ
            DiningTable table4 = new DiningTable(
                DiningTable.TableStatus.AVAILABLE,
                DiningTable.TableCondition.GOOD,
                now, now, 6, 0
            );

            // Bàn 5 - OCCUPIED (Có khách) - 4 chỗ
            DiningTable table5 = new DiningTable(
                DiningTable.TableStatus.OCCUPIED,
                DiningTable.TableCondition.GOOD,
                now, now, 4, 0
            );

            // Bàn 6 - AVAILABLE (Bàn trống) - 4 chỗ
            DiningTable table6 = new DiningTable(
                DiningTable.TableStatus.AVAILABLE,
                DiningTable.TableCondition.GOOD,
                now, now, 4, 0
            );

            // Bàn 7 - AVAILABLE (Đã xếp trước) - 6 chỗ
            DiningTable table7 = new DiningTable(
                DiningTable.TableStatus.AVAILABLE,
                DiningTable.TableCondition.GOOD,
                now, now, 6, 0
            );

            // Bàn 8 - AVAILABLE (Bàn trống) - 2 chỗ
            DiningTable table8 = new DiningTable(
                DiningTable.TableStatus.AVAILABLE,
                DiningTable.TableCondition.GOOD,
                now, now, 2, 0
            );

            // Bàn 9 - WAITING_PAYMENT (Chờ thanh toán) - 4 chỗ
            DiningTable table9 = new DiningTable(
                DiningTable.TableStatus.WAITING_PAYMENT,
                DiningTable.TableCondition.GOOD,
                now, now, 4, 0
            );

            // Bàn 10 - AVAILABLE (Bàn trống) - 4 chỗ
            DiningTable table10 = new DiningTable(
                DiningTable.TableStatus.AVAILABLE,
                DiningTable.TableCondition.NEW,
                now, now, 4, 0
            );

            // Save tables to database
            tableService.add(table1);
            tableService.add(table2);
            tableService.add(table3);
            tableService.add(table4);
            tableService.add(table5);
            tableService.add(table6);
            tableService.add(table7);
            tableService.add(table8);
            tableService.add(table9);
            tableService.add(table10);
        };
    }
}

package com.group1.swp.pizzario_swp391;


import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.service.StaffService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class PizzarIoSwp391Application {

    public static void main(String[] args) {
        SpringApplication.run(PizzarIoSwp391Application.class, args);
    }

    //@Bean
    CommandLineRunner initData(StaffService service) {
        return (String[] args) -> {
//            System.out.println("Runner chạy với: " + java.util.Arrays.toString(args));

            // 3 nhân viên mẫu
            Staff s1 = new Staff(
                    "Nguyen Van A",
                    java.time.LocalDate.of(1995, 5, 10),
                    "0901234567",
                    "Hà Nội",
                    "cashier01",
                    "123456",
                    "hhcc782005@gmail.com",
                    Staff.Role.CASHIER,
                    true
            );

            Staff s2 = new Staff(
                    "Tran Thi B",
                    java.time.LocalDate.of(1992, 8, 20),
                    "0907654321",
                    "TP.HCM",
                    "manager01",
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
                    "kitchen01",
                    "123456",
                    "hiep@gmail.com",
                    Staff.Role.KITCHEN,
                    true
            );


            // Lưu dữ liệu
            service.add(s1);
            service.add(s2);
            service.add(s3);

            // Nếu bạn vẫn muốn tạo Account riêng, giữ các dòng dưới (tuỳ hệ thống của bạn):
            // Account account4 = new Account("huy@gmail.com", "123456", "MANAGER");
            // Account account1 = new Account("hai@gmail.com", "123456", "CASHIER");
            // Account account2 = new Account("hiep@gmail.com", "123456", "KITCHEN");
            // service.register(account4);
            // service.register(account1);
            // service.register(account2);
        };
    }
}

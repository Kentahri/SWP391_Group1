package com.group1.swp.pizzario_swp391;

import com.group1.swp.pizzario_swp391.entity.Account;
import com.group1.swp.pizzario_swp391.service.LoginService;
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
    CommandLineRunner initData(LoginService service) {
        return (String[] args) -> {
            System.out.println("Runner chạy với: " + java.util.Arrays.toString(args));


            Account account4 = new Account("huy@gmail.com", "123456", "MANAGER");
            Account account1 = new Account("hai@gmail.com", "123456", "CASHIER");
            Account account2 = new Account("hiep@gmail.com", "123456", "KITCHEN");
            service.register(account4);
            service.register(account1);
            service.register(account2);
            // TODO: code khởi tạo dữ liệu ở đây
        };
    }
}

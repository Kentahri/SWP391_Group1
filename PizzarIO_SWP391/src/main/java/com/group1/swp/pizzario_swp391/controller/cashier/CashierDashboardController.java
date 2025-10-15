package com.group1.swp.pizzario_swp391.controller.cashier;

import java.security.Principal;
import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group1.swp.pizzario_swp391.dto.order.OrderDetailDTO;
import com.group1.swp.pizzario_swp391.dto.reservation.ReservationCreateDTO;
import com.group1.swp.pizzario_swp391.dto.reservation.ReservationDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableForCashierDTO;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.service.ReservationService;
import com.group1.swp.pizzario_swp391.service.StaffService;
import com.group1.swp.pizzario_swp391.service.TableService;

import jakarta.validation.Valid;

/**
 * Cashier Dashboard Controller
 * Handle HTTP requests for cashier page with traditional POST/GET
 */
@Controller
@RequestMapping("/cashier")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CashierDashboardController {

    TableService tableService;
    StaffService staffService;
    ReservationService reservationService;

    @GetMapping
    public String cashierDashboard(Model model, Principal principal) {
        try {
            String email = principal.getName();
            Staff staff = staffService.findByEmail(email);

            List<TableForCashierDTO> tables = tableService.getTablesForCashier();
            model.addAttribute("staff", staff);
            model.addAttribute("tables", tables);
            model.addAttribute("reservationCreateDTO", new ReservationCreateDTO());
            return "cashier-page/cashier-dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải dữ liệu. Vui lòng thử lại.");
            return "error-page";
        }
    }

    /**
     * Xem chi tiết order của bàn
     */
    @GetMapping("/tables/{id}/order")
    public String getTableOrderDetail(@PathVariable("id") Integer tableId, Model model, Principal principal) {
        try {
            String email = principal.getName();
            Staff staff = staffService.findByEmail(email);

            OrderDetailDTO orderDetail = null;
            orderDetail = tableService.getOrderDetailByTableId(tableId);
            List<TableForCashierDTO> tables = tableService.getTablesForCashier();

            model.addAttribute("staff", staff);
            model.addAttribute("tables", tables);
            model.addAttribute("selectedTableId", tableId);
            model.addAttribute("orderDetail", orderDetail);
            model.addAttribute("reservationCreateDTO", new ReservationCreateDTO());
            model.addAttribute("tableCapacity", tableService.getTableById(tableId).getCapacity());
            return "cashier-page/cashier-dashboard";
        } catch (Exception _) {
            model.addAttribute("error", "Không thể tải thông tin bàn. Vui lòng thử lại.");
            return "error-page";
        }
    }

    /**
     * Tạo reservation mới
     */
    @PostMapping("/reservations")
    public String createReservation(
            @Valid @ModelAttribute("reservationCreateDTO") ReservationCreateDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model,
            Principal principal) {

        // Kiểm tra xem có đủ dữ liệu để validate business logic không (tránh NPE)
        boolean canValidateBusinessLogic = dto.getTableId() != null 
                                        && dto.getStartTime() != null 
                                        && dto.getCapacityExpected() > 0;

        // Nếu có đủ dữ liệu, thử validate business logic để catch cả DTO errors và business errors
        if (canValidateBusinessLogic) {
            try {
                ReservationDTO reservation = reservationService.createReservation(dto);
                
                // Chỉ redirect thành công nếu KHÔNG có bất kỳ validation error nào
                if (!bindingResult.hasErrors()) {
                    redirectAttributes.addFlashAttribute("successMessage", "Đặt bàn thành công cho khách " + reservation.getCustomerName());
                    return "redirect:/cashier";
                }
            } catch (RuntimeException e) {
                // Map business logic errors to specific fields
                String errorMessage = e.getMessage();
                if (errorMessage.contains("Vượt quá số người tối đa") || errorMessage.contains("số người")) {
                    bindingResult.rejectValue("capacityExpected", "error.capacityExpected", errorMessage);
                } else if (errorMessage.contains("Bàn đã được đặt") || errorMessage.contains("thời gian này")) {
                    bindingResult.rejectValue("startTime", "error.startTime", errorMessage);
                } else if (errorMessage.contains("90 phút") || errorMessage.contains("cách nhau")) {
                    bindingResult.rejectValue("startTime", "error.startTime", errorMessage);
                }
                // Không set general errorMessage - chỉ inline errors
            }
        }

        // Hiển thị form với TẤT CẢ errors (DTO validation + business logic)
        if (bindingResult.hasErrors()) {
            String email = principal.getName();
            Staff staff = staffService.findByEmail(email);
            List<TableForCashierDTO> tables = tableService.getTablesForCashier();
            
            model.addAttribute("staff", staff);
            model.addAttribute("tables", tables);
            model.addAttribute("showReservationModal", true);
            
            return "cashier-page/cashier-dashboard";
        }

        // Không có errors - redirect về dashboard (fallback)
        return "redirect:/cashier";
    }

    /**
     * Xem danh sách reservation sắp tới
     */
    @GetMapping("/reservations/upcoming")
    public String getUpcomingReservations(Model model, Principal principal) {
        try {
            String email = principal.getName();
            Staff staff = staffService.findByEmail(email);

            List<ReservationDTO> upcomingReservations = reservationService.getUpcomingReservations();
            List<TableForCashierDTO> tables = tableService.getTablesForCashier();

            model.addAttribute("staff", staff);
            model.addAttribute("tables", tables);
            model.addAttribute("upcomingReservations", upcomingReservations);
            model.addAttribute("reservationCreateDTO", new ReservationCreateDTO());
            return "cashier-page/cashier-dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải danh sách đặt bàn. Vui lòng thử lại.");
            return "error-page";
        }
    }

    /**
     * Xem reservation của một bàn cụ thể
     */
    @GetMapping("/tables/{tableId}/reservations")
    public String getTableReservations(@PathVariable Integer tableId, Model model, Principal principal) {
        try {
            String email = principal.getName();
            Staff staff = staffService.findByEmail(email);

            List<ReservationDTO> tableReservations = reservationService.getReservationsByTableId(tableId);
            List<TableForCashierDTO> tables = tableService.getTablesForCashier();

            model.addAttribute("staff", staff);
            model.addAttribute("tables", tables);
            model.addAttribute("selectedTableId", tableId);
            model.addAttribute("tableReservations", tableReservations);
            model.addAttribute("reservationCreateDTO", new ReservationCreateDTO());
            return "cashier-page/cashier-dashboard";
        } catch (Exception _) {
            model.addAttribute("error", "Không thể tải danh sách đặt bàn. Vui lòng thử lại.");
            return "error-page";
        }
    }

    /**
     * Hủy reservation
     */
    @PostMapping("/reservations/{id}/delete")
    public String cancelReservation(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false) String returnUrl) {
        try {
            reservationService.cancelReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đặt bàn thành công");

            // Redirect về URL trước đó nếu có, không thì về dashboard
            if (returnUrl != null && !returnUrl.isEmpty()) {
                return "redirect:" + returnUrl;
            }
            return "redirect:/cashier";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cashier";
        } catch (Exception _) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi. Vui lòng thử lại.");
            return "redirect:/cashier";
        }
    }
}



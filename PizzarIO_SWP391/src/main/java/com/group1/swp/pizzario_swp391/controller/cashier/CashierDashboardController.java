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
import com.group1.swp.pizzario_swp391.dto.reservation.ReservationUpdateDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableForCashierDTO;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.mapper.ReservationMapper;
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
    ReservationMapper reservationMapper;

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

        try {
            reservationService.validateReservationBusinessLogic(dto);
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Vượt quá số người tối đa") || errorMessage.contains("số người")) {
                bindingResult.rejectValue("capacityExpected", "error.capacityExpected", errorMessage);
            } else if (errorMessage.contains("Bàn đã được đặt") || errorMessage.contains("thời gian này")) {
                bindingResult.rejectValue("startTime", "error.startTime", errorMessage);
            } else if (errorMessage.contains("90 phút") || errorMessage.contains("cách nhau")) {
                bindingResult.rejectValue("startTime", "error.startTime", errorMessage);
            } else if (errorMessage.contains("Không tìm thấy bàn")) {
                bindingResult.rejectValue("tableId", "error.tableId", errorMessage);
            }
        }

        if (bindingResult.hasErrors()) {
            String email = principal.getName();
            Staff staff = staffService.findByEmail(email);
            List<TableForCashierDTO> tables = tableService.getTablesForCashier();

            model.addAttribute("staff", staff);
            model.addAttribute("tables", tables);
            model.addAttribute("showReservationModal", true);

            return "cashier-page/cashier-dashboard";
        }

        try {
            ReservationDTO reservation = reservationService.createReservation(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt bàn thành công cho khách " + reservation.getCustomerName());
            return "redirect:/cashier";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/cashier";
        }
    }
    /**
     * Xem danh sách reservation sắp tới (có hỗ trợ tìm kiếm)
     */
    @GetMapping("/reservations/upcoming")
    public String getUpcomingReservations(
            @RequestParam(required = false) String search,
            Model model,
            Principal principal) {
        try {
            String email = principal.getName();
            Staff staff = staffService.findByEmail(email);

            // Nếu có từ khóa tìm kiếm thì search, không thì lấy tất cả
            List<ReservationDTO> upcomingReservations;
            if (search != null && !search.trim().isEmpty()) {
                upcomingReservations = reservationService.searchUpcomingReservations(search);
                model.addAttribute("searchKeyword", search);
            } else {
                upcomingReservations = reservationService.getUpcomingReservations();
            }

            List<TableForCashierDTO> tables = tableService.getTablesForCashier();

            model.addAttribute("staff", staff);
            model.addAttribute("tables", tables);
            model.addAttribute("upcomingReservations", upcomingReservations);
            model.addAttribute("reservationCreateDTO", new ReservationCreateDTO());
            return "cashier-page/cashier-dashboard";
        } catch (Exception _) {
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
     * Lấy form chỉnh sửa reservation
     */
    @GetMapping("/reservations/{id}/edit")
    public String editReservation(
            @PathVariable Long id,
            Model model,
            Principal principal,
            @RequestParam(required = false) String returnUrl) {
        try {
            String email = principal.getName();
            Staff staff = staffService.findByEmail(email);

            ReservationDTO reservation = reservationService.findById(id);
            List<TableForCashierDTO> tables = tableService.getTablesForCashier();

            ReservationUpdateDTO updateDTO = reservationMapper.toReservationUpdateDTO(reservation);

            model.addAttribute("staff", staff);
            model.addAttribute("tables", tables);
            model.addAttribute("reservationUpdateDTO", updateDTO);
            model.addAttribute("showUpdateModal", true);
            model.addAttribute("returnUrl", returnUrl);

            List<ReservationDTO> upcomingReservations = reservationService.getUpcomingReservations();
            model.addAttribute("upcomingReservations", upcomingReservations);
            model.addAttribute("reservationCreateDTO", new ReservationCreateDTO());

            return "cashier-page/cashier-dashboard";
        } catch (Exception _) {
            model.addAttribute("error", "Không thể tải thông tin đặt bàn. Vui lòng thử lại.");
            return "error-page";
        }
    }

    /**
     * Cập nhật reservation
     */
    @PostMapping("/reservations/{id}/update")
    public String updateReservation(
            @PathVariable Long id,
            @Valid @ModelAttribute("reservationUpdateDTO") ReservationUpdateDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model,
            Principal principal,
            @RequestParam(required = false) String returnUrl) {

        boolean canValidateBusinessLogic = dto.getStartTime() != null && dto.getCapacityExpected() > 0;

        if (canValidateBusinessLogic) {
            try {
                ReservationDTO updated = reservationService.updateReservation(id, dto);

                if (!bindingResult.hasErrors()) {
                    redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đặt bàn thành công cho khách " + updated.getCustomerName());

                    if (returnUrl != null && !returnUrl.isEmpty()) {
                        return "redirect:" + returnUrl;
                    }
                    return "redirect:/cashier/reservations/upcoming";
                }
            } catch (RuntimeException e) {
                String errorMessage = e.getMessage();
                if (errorMessage.contains("Vượt quá số người tối đa") || errorMessage.contains("số người")) {
                    bindingResult.rejectValue("capacityExpected", "error.capacityExpected", errorMessage);
                } else if (errorMessage.contains("Bàn đã được đặt") || errorMessage.contains("thời gian này")) {
                    bindingResult.rejectValue("startTime", "error.startTime", errorMessage);
                } else if (errorMessage.contains("90 phút") || errorMessage.contains("cách nhau")) {
                    bindingResult.rejectValue("startTime", "error.startTime", errorMessage);
                } else if (errorMessage.contains("đã bị hủy")) {
                    bindingResult.reject("error.reservation", errorMessage);
                }
            }
        }

        if (bindingResult.hasErrors()) {
            String email = principal.getName();
            Staff staff = staffService.findByEmail(email);
            List<TableForCashierDTO> tables = tableService.getTablesForCashier();
            List<ReservationDTO> upcomingReservations = reservationService.getUpcomingReservations();

            model.addAttribute("staff", staff);
            model.addAttribute("tables", tables);
            model.addAttribute("upcomingReservations", upcomingReservations);
            model.addAttribute("reservationCreateDTO", new ReservationCreateDTO());
            model.addAttribute("showUpdateModal", true);
            model.addAttribute("returnUrl", returnUrl);

            return "cashier-page/cashier-dashboard";
        }

        return "redirect:/cashier/reservations/upcoming";
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

    /**
     * Mở bàn cho khách đã đặt trước (khách đã đến)
     */
    @PostMapping("/reservations/{id}/open")
    public String openTableForReservation(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false) String returnUrl) {
        try {
            reservationService.openTableForGuestWithReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã mở bàn cho khách. Chúc phục vụ vui vẻ!");
            
            if (returnUrl != null && !returnUrl.isEmpty()) {
                return "redirect:" + returnUrl;
            }
            return "redirect:/cashier";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            if (returnUrl != null && !returnUrl.isEmpty()) {
                return "redirect:" + returnUrl;
            }
            return "redirect:/cashier";
        } catch (Exception _) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi. Vui lòng thử lại.");
            return "redirect:/cashier";
        }
    }
}



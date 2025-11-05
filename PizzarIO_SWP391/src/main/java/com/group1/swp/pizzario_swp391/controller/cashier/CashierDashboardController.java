package com.group1.swp.pizzario_swp391.controller.cashier;

import java.security.Principal;
import java.util.List;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group1.swp.pizzario_swp391.annotation.CashierUrl;
import com.group1.swp.pizzario_swp391.dto.order.OrderDetailDTO;
import com.group1.swp.pizzario_swp391.dto.order.UpdateOrderItemsDTO;
import com.group1.swp.pizzario_swp391.dto.reservation.ReservationCreateDTO;
import com.group1.swp.pizzario_swp391.dto.reservation.ReservationDTO;
import com.group1.swp.pizzario_swp391.dto.reservation.ReservationUpdateDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableForCashierDTO;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.mapper.ReservationMapper;
import com.group1.swp.pizzario_swp391.service.OrderService;
import com.group1.swp.pizzario_swp391.service.ReservationService;
import com.group1.swp.pizzario_swp391.service.StaffService;
import com.group1.swp.pizzario_swp391.service.TableService;
import com.group1.swp.pizzario_swp391.service.ProductService;
import com.group1.swp.pizzario_swp391.service.CategoryService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Cashier Dashboard Controller
 * Handle HTTP requests for cashier page with traditional POST/GET
 */
@CashierUrl
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CashierDashboardController {

    TableService tableService;
    StaffService staffService;
    ReservationService reservationService;
    ReservationMapper reservationMapper;
    OrderService orderService;
    ProductService productService;
    CategoryService categoryService;

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

            OrderDetailDTO orderDetail = tableService.getOrderDetailByTableId(tableId);
            List<TableForCashierDTO> tables = tableService.getTablesForCashier();

            model.addAttribute("staff", staff);
            model.addAttribute("tables", tables);
            model.addAttribute("selectedTableId", tableId);
            model.addAttribute("orderDetail", orderDetail);
            model.addAttribute("reservationCreateDTO", new ReservationCreateDTO());

            var tableInfo = tableService.getTableById(tableId);
            model.addAttribute("tableCapacity", tableInfo.getCapacity());
            model.addAttribute("tableStatus", tableInfo.getTableStatus());

            // Load products and categories for edit order panel
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("categories", categoryService.getAllCategories());

            return "cashier-page/cashier-dashboard";
        } catch (Exception e) {
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
            reservationService.validateReservationBusinessLogicForCreate(dto);
        } catch (com.group1.swp.pizzario_swp391.exception.ValidationException e) {
            e.getFieldErrors().forEach((field, message) -> {
                bindingResult.rejectValue(field, "error." + field, message);
            });
        } catch (RuntimeException e) {
            bindingResult.reject("error.reservation", e.getMessage());
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
            redirectAttributes.addFlashAttribute("successMessage", "Đặt bàn thành công cho khách " + reservation.getCustomerName() + " tại bàn " + dto.getTableId());
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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

        // Kiểm tra business logic ngay cả khi có lỗi validation cơ bản
        boolean canValidateBusinessLogic = dto.getStartTime() != null && dto.getCapacityExpected() > 0;

        if (canValidateBusinessLogic) {
            try {
                reservationService.validateReservationBusinessLogicForUpdate(id, dto);

                // Nếu validation thành công, thực hiện update
                ReservationDTO updated = reservationService.updateReservation(id, dto);

                // Nếu không có lỗi nào, redirect thành công
                if (!bindingResult.hasErrors()) {
                    redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đặt bàn thành công cho khách " + updated.getCustomerName());

                    if (returnUrl != null && !returnUrl.isEmpty()) {
                        return "redirect:" + returnUrl;
                    }
                    return "redirect:/cashier/reservations/upcoming";
                }
            } catch (com.group1.swp.pizzario_swp391.exception.ValidationException e) {
                e.getFieldErrors().forEach((field, message) -> {
                    bindingResult.rejectValue(field, "error." + field, message);
                });
            } catch (RuntimeException e) {
                bindingResult.reject("error.reservation", e.getMessage());
            }
        }

        // Render lại form với tất cả lỗi (validation + business logic)
        if (bindingResult.hasErrors()) {
            String email = principal.getName();
            Staff staff = staffService.findByEmail(email);
            List<TableForCashierDTO> tables = tableService.getTablesForCashier();
            List<ReservationDTO> upcomingReservations = reservationService.getUpcomingReservations();

            // Sử dụng DTO từ form để giữ lại các lỗi validation
            if (dto.getTableId() == null) {
                ReservationDTO reservation = reservationService.findById(id);
                dto.setTableId(reservation.getTableId());
            }

            model.addAttribute("staff", staff);
            model.addAttribute("tables", tables);
            model.addAttribute("upcomingReservations", upcomingReservations);
            model.addAttribute("reservationCreateDTO", new ReservationCreateDTO());
            model.addAttribute("reservationUpdateDTO", dto);
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi. Vui lòng thử lại.");
            return "redirect:/cashier";
        }
    }

    /**
     * Xem lịch sử hóa đơn
     */
    @GetMapping("/history")
    public String viewPaymentHistory(Model model, Principal principal) {
        try {
            String email = principal.getName();
            Staff staff = staffService.findByEmail(email);

            List<TableForCashierDTO> tables = tableService.getTablesForCashier();
            var paymentHistory = orderService.getPaymentHistory();

            model.addAttribute("staff", staff);
            model.addAttribute("tables", tables);
            model.addAttribute("paymentHistory", paymentHistory);
            model.addAttribute("showHistory", true);
            model.addAttribute("reservationCreateDTO", new ReservationCreateDTO());
            return "cashier-page/cashier-dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải lịch sử hóa đơn. Vui lòng thử lại.");
            return "error-page";
        }
    }

    /**
     * Xem chi tiết order từ lịch sử
     */
    @GetMapping("/history/{orderId}")
    public String viewOrderDetail(@PathVariable Long orderId, Model model, Principal principal) {
        try {
            String email = principal.getName();
            Staff staff = staffService.findByEmail(email);

            // Lấy chi tiết order từ payment history
            var paymentHistory = orderService.getPaymentHistory();
            var orderDetail = paymentHistory.stream()
                    .filter(order -> order.getOrderId().equals(orderId))
                    .findFirst()
                    .orElse(null);

            if (orderDetail == null) {
                model.addAttribute("error", "Không tìm thấy đơn hàng này trong lịch sử.");
                return "error-page";
            }

            model.addAttribute("staff", staff);
            model.addAttribute("orderDetail", orderDetail);
            return "cashier-page/order-detail";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải chi tiết đơn hàng. Vui lòng thử lại.");
            return "error-page";
        }
    }

    /**
     * Khóa bàn để gộp bàn
     * Chuyển trạng thái từ AVAILABLE → LOCKED
     */
    @PostMapping("/tables/{tableId}/lock")
    public String lockTableForMerge(
            @PathVariable Integer tableId,
            RedirectAttributes redirectAttributes) {
        try {
            tableService.lockTableForMerge(tableId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã khóa bàn " + tableId + " thành công!");
            return "redirect:/cashier/tables/" + tableId + "/order";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cashier/tables/" + tableId + "/order";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi. Vui lòng thử lại.");
            return "redirect:/cashier/tables/" + tableId + "/order";
        }
    }

    /**
     * Mở khóa bàn đã bị khóa (từ gộp bàn)
     * Chuyển trạng thái từ LOCKED → AVAILABLE
     */
    @PostMapping("/tables/{tableId}/unlock")
    public String unlockTableFromMerge(
            @PathVariable Integer tableId,
            RedirectAttributes redirectAttributes) {
        try {
            tableService.unlockTableFromMerge(tableId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã mở khóa bàn " + tableId + " thành công!");
            return "redirect:/cashier/tables/" + tableId + "/order";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cashier/tables/" + tableId + "/order";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi. Vui lòng thử lại.");
            return "redirect:/cashier/tables/" + tableId + "/order";
        }
    }

    /**
     * Cập nhật order items (thêm/xóa/sửa món)
     */
    @PostMapping("/tables/{id}/order/update")
    public String updateOrderItems(
            @PathVariable("id") Integer tableId,
            @RequestParam("items") String itemsJson,
            @RequestParam(value = "orderId", required = false) Long orderId,
            RedirectAttributes redirectAttributes) {
        try {
            // Get order by table
            if (orderId == null) {
                OrderDetailDTO orderDetail = tableService.getOrderDetailByTableId(tableId);
                if (orderDetail == null || orderDetail.getOrderId() == null) {
                    throw new RuntimeException("Không tìm thấy order cho bàn này");
                }
                orderId = orderDetail.getOrderId();
            }

            // Parse JSON string to list of items
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.core.type.TypeReference<List<UpdateOrderItemsDTO.OrderItemUpdate>> typeRef =
                    new com.fasterxml.jackson.core.type.TypeReference<List<UpdateOrderItemsDTO.OrderItemUpdate>>() {};
            List<UpdateOrderItemsDTO.OrderItemUpdate> items = objectMapper.readValue(itemsJson, typeRef);

            // Call service to update order
            orderService.updateOrderItemsForCashier(orderId, items);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật order thành công!");
            return "redirect:/cashier/tables/" + tableId + "/order";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/cashier/tables/" + tableId + "/order";
        }
    }
}



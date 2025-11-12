package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftResponseDTO;
import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.mapper.StaffShiftMapper;
import com.group1.swp.pizzario_swp391.repository.StaffShiftRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffShiftExcelExportService {

    private final StaffShiftRepository staffShiftRepository;
    private final StaffShiftMapper staffShiftMapper;

    public byte[] generateMonthlyReport(int year, int month) throws IOException{

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<StaffShift> staffShifts = staffShiftRepository.findByMonthRange(monthStart, monthEnd);
        List<StaffShiftResponseDTO> dtos = staffShifts.stream()
                .map(staffShiftMapper::toResponseDTO)
                .collect(Collectors.toList());

        Map<Integer, List<StaffShiftResponseDTO>> staffGroups = dtos.stream()
                .collect(Collectors.groupingBy(StaffShiftResponseDTO::getStaffId));

        Workbook workbook = new XSSFWorkbook();
        Sheet summarySheet = workbook.createSheet("Tổng hợp");

        createSummarySheet(summarySheet, staffGroups, year, month, workbook);

        for (Map.Entry<Integer, List<StaffShiftResponseDTO>> entry : staffGroups.entrySet()){
            Integer staffId = entry.getKey();
            List<StaffShiftResponseDTO> staffData = entry.getValue();
            String staffName = staffData.get(0).getStaffName();

            Sheet sheet = workbook.createSheet(staffName);
            createStaffSheet(sheet, staffName, staffData, year, month, workbook);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    private void createStaffSheet(Sheet sheet, String staffName, List<StaffShiftResponseDTO> data,
                                  int year, int month, Workbook workbook) {
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle totalStyle = createTotalStyle(workbook);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM/yyyy");
        String monthString = monthFormatter.format(LocalDate.of(year, month, 1));

        // Title
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("LƯƠNG THÁNG " + monthString + " - " + staffName);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

        // Headers
        Row headerRow = sheet.createRow(2);
        String[] headers = {"Ngày", "Ca làm việc", "Trạng thái", "Giờ bắt đầu", "Giờ kết thúc",
                "Lương ca", "Phạt (%)", "Lương thực tế", "Ghi chú"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        int rowNum = 3;
        double totalActualWage = 0;

        for (StaffShiftResponseDTO dto : data) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(dto.getWorkDate().format(formatter)); // Ngày
            row.createCell(1).setCellValue(dto.getShiftName()); // Ca làm việc
            row.createCell(2).setCellValue(getStatusText(dto.getShiftStatus())); // Trạng thái

            // Giờ bắt đầu và kết thúc
            String startTime = dto.getStartTime() != null
                    ? dto.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "";
            String endTime = dto.getEndTime() != null
                    ? dto.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "";
            row.createCell(3).setCellValue(startTime);
            row.createCell(4).setCellValue(endTime);

            // Lương ca
            double baseWage = dto.getHourlyWage() != null ? dto.getHourlyWage().doubleValue() : 0;
            row.createCell(5).setCellValue(baseWage);

            // Phạt %
            int penaltyPercent = dto.getPenaltyPercent() != null ? dto.getPenaltyPercent() : 0;
            row.createCell(6).setCellValue(penaltyPercent + "%");

            // Lương thực tế
            double actualWage = calculateActualWage(dto);
            row.createCell(7).setCellValue(actualWage);
            totalActualWage += actualWage;

            // Ghi chú
            row.createCell(8).setCellValue(dto.getNote() != null ? dto.getNote() : "");

            for (int i = 0; i < 9; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        // Total row
        Row totalRow = sheet.createRow(rowNum);
        totalRow.createCell(0).setCellValue("TỔNG CỘNG");
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 6));
        Cell totalCell = totalRow.createCell(7);
        totalCell.setCellValue(totalActualWage);
        totalCell.setCellStyle(totalStyle);

        // Auto-size columns
        for (int i = 0; i < 9; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSummarySheet(Sheet summarySheet, Map<Integer, List<StaffShiftResponseDTO>> staffGroups, int year, int month, Workbook workbook) {
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle totalStyle = createTotalStyle(workbook);

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String monthString = monthFormatter.format(LocalDate.of(year, month, 1));

        Row titleRow = summarySheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("TỔNG HỢP LƯƠNG THÁNG " + monthString);
        titleCell.setCellStyle(titleStyle);

        Row headerRow = summarySheet.createRow(2);
        String[] headers = {"STT", "Tên nhân viên", "Số ca", "Tổng giờ", "Tổng lương", "Lương thực tế"};

        for (int i = 0; i < headers.length; i++){
            Cell cellHeader = headerRow.createCell(i);

            cellHeader.setCellValue(headers[i]);
            cellHeader.setCellStyle(headerStyle);
        }

        // Data rows
        int rowNum = 3;
        double grandTotal = 0;

        for (Map.Entry<Integer, List<StaffShiftResponseDTO>> entry : staffGroups.entrySet()) {
            List<StaffShiftResponseDTO> staffData = entry.getValue();
            StaffSummary summary = calculateSummary(staffData);

            Row row = summarySheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowNum - 3); // STT
            row.createCell(1).setCellValue(staffData.get(0).getStaffName());
            row.createCell(2).setCellValue(summary.shiftCount);
            row.createCell(3).setCellValue(summary.totalHours);
            row.createCell(4).setCellValue(summary.totalWage);
            row.createCell(5).setCellValue(summary.actualWage);

            for (int i = 0; i < 6; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }

            grandTotal += summary.actualWage;
        }

    }

    private StaffSummary calculateSummary(List<StaffShiftResponseDTO> data) {
        int shiftCount = data.size();
        double totalHours = 0;
        double totalWage = 0;
        int totalPenalty = 0;
        double actualWage = 0;

        for (StaffShiftResponseDTO dto : data) {
            if ("COMPLETED".equals(dto.getShiftStatus())) {
                if (dto.getStartTime() != null && dto.getEndTime() != null) {
                    totalHours += Duration.between(dto.getStartTime(), dto.getEndTime()).toHours();
                }
            }

            if (dto.getPenaltyPercent() != null) {
                totalPenalty += dto.getPenaltyPercent();
            }

            actualWage += calculateActualWage(dto);
        }

        return new StaffSummary(shiftCount, totalHours, totalWage,
                totalPenalty / data.size(), actualWage);
    }

    public double calculateActualWage(StaffShiftResponseDTO dto) {
        if (dto.getShiftStatus() == null || dto.getHourlyWage() == null) {
            return 0.0;
        }

        double baseWage = dto.getHourlyWage().doubleValue();
        int penaltyPercent = dto.getPenaltyPercent() != null ? dto.getPenaltyPercent() : 0;
        double multiplier = (100 - penaltyPercent) / 100.0;

        if ("COMPLETED".equals(dto.getShiftStatus())) {
            long hours = Duration.between(dto.getStartTime(), dto.getEndTime()).toHours();
            return baseWage * hours * multiplier;
        } else if ("LEFT_EARLY".equals(dto.getShiftStatus())
                && dto.getCheckIn() != null && dto.getCheckOut() != null) {
            long hours = Duration.between(dto.getCheckIn(), dto.getCheckOut()).toHours();
            return baseWage * hours * multiplier;
        }
        else if ("LATE".equals(dto.getShiftStatus())) {
            long hours = Duration.between(dto.getStartTime(), dto.getEndTime()).toHours();
            return baseWage * hours * multiplier;
        }
        return 0.0;
    }

    private String getStatusText(String status) {
        if (status == null) return "Không xác định";
        return switch (status) {
            case "COMPLETED" -> "Hoàn thành";
            case "SCHEDULED" -> "Đã lên lịch";
            case "PRESENT" -> "Có mặt";
            case "LATE" -> "Đi muộn";
            case "ABSENT" -> "Vắng mặt";
            case "LEFT_EARLY" -> "Về sớm";
            case "NOT_CHECKOUT" -> "Chưa checkout";
            default -> "Không xác định";
        };
    }

    private CellStyle createTotalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static class StaffSummary{
        int shiftCount;
        double totalHours;
        double totalWage;
        int totalPenalty;
        double actualWage;

        public StaffSummary(int shiftCount, double totalHours, double totalWage, int totalPenalty, double actualWage) {
            this.shiftCount = shiftCount;
            this.totalHours = totalHours;
            this.totalWage = totalWage;
            this.totalPenalty = totalPenalty;
            this.actualWage = actualWage;
        }
    }
}

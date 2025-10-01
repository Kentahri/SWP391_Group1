package com.group1.swp.pizzario_swp391.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.group1.swp.pizzario_swp391.dto.table.TableCreateDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableForCashierDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableManagementDTO;
import com.group1.swp.pizzario_swp391.entity.DiningTable;
import com.group1.swp.pizzario_swp391.mapper.TableMapper;
import com.group1.swp.pizzario_swp391.repository.TableRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TableService {
    TableRepository tableRepository;
    TableMapper tableMapper;

    /**
     * Tạo bàn mới (Manager)
     * Manager chỉ nhập capacity, hệ thống tự set status=AVAILABLE và condition=NEW
     */
    public void createNewTable(TableCreateDTO tableCreateDTO) {
        DiningTable diningTable = tableMapper.toDiningTable(tableCreateDTO);
        LocalDateTime now = LocalDateTime.now();
        
        // Tự động set các giá trị mặc định
        diningTable.setTableStatus(DiningTable.TableStatus.AVAILABLE);
        diningTable.setTableCondition(DiningTable.TableCondition.NEW);
        diningTable.setCreatedAt(now);
        diningTable.setUpdatedAt(now);
        
        tableRepository.save(diningTable);
    }

    /**
     * Lấy tất cả bàn (Manager)
     */
    public List<TableDTO> getAllTables() {
        return tableMapper.toTableDTOs(tableRepository.findAll());
    }

    /**
     * Lấy bàn theo ID
     */
    public TableDTO getTableById(Integer id) {
        return tableMapper.toTableDTO(tableRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Table not found")));
    }

    /**
     * Cập nhật bàn (Manager)
     * Manager chỉ cập nhật capacity và tableCondition
     * TableStatus do Cashier quản lý
     */
    public void updateTable(int id, TableManagementDTO tableManagementDTO) {
        DiningTable table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found"));
        tableMapper.updateDiningTable(table, tableManagementDTO);
        table.setUpdatedAt(LocalDateTime.now());
        tableRepository.save(table);
    }

    /**
     * Xóa bàn (Manager)
     */
    public void deleteTable(int id) {
        tableRepository.deleteById(id);
    }


    /**
     * Lấy danh sách bàn cho Cashier
     */
    public List<TableForCashierDTO> getTablesForCashier() {
        return tableMapper.toTableForCashierDTOs(tableRepository.findAll());
    }

    /**
     * Cập nhật trạng thái bàn (Cashier)
     * Cashier chỉ được cập nhật tableStatus
     */
    public void updateTableStatus(Integer id, DiningTable.TableStatus status) {
        DiningTable table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found"));
        table.setTableStatus(status);
        table.setUpdatedAt(LocalDateTime.now());
        tableRepository.save(table);
    }
}
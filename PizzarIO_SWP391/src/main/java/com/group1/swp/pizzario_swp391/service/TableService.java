package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.table.TableDTO;
import com.group1.swp.pizzario_swp391.entity.DiningTable;
import com.group1.swp.pizzario_swp391.mapper.TableMapper;
import com.group1.swp.pizzario_swp391.repository.TableRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TableService {
    TableRepository tableRepository;
    TableMapper tableMapper;

    public void createNewTable(TableDTO tableDTO) {
        LocalDateTime now = LocalDateTime.now();
        tableDTO.setCreatedAt(now);
        tableDTO.setUpdatedAt(now);
        tableDTO.setTableStatus(DiningTable.TableStatus.AVAILABLE);
        tableDTO.setTableCondition(DiningTable.TableCondition.NEW);
        DiningTable diningTable = tableMapper.toDiningTable(tableDTO);

        tableRepository.save(diningTable);
    }

    public java.util.List<DiningTable> getAllTables() {
        return tableRepository.findAll();
    }

    public java.util.Optional<DiningTable> getTableById(Integer id) {
        return tableRepository.findById(id);
    }

    public java.util.Optional<DiningTable> updateTableStatus(Integer id, DiningTable.TableStatus status) {
        return tableRepository.findById(id).map(t -> {
            if (status != null) t.setTableStatus(status);
            return tableRepository.save(t);
        });
    }

    public void deleteTable(int id) {
        tableRepository.deleteById(id);
    }

    public void updateTable(int id, TableDTO tableDTO) {
        DiningTable table = tableRepository.findById(id).orElseThrow(() -> new RuntimeException("Table not found"));
        tableMapper.updateDiningTable(table, tableDTO);
        table.setUpdatedAt(LocalDateTime.now());
        tableRepository.save(table);
    }

}

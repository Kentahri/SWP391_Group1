package com.group1.swp.pizzario_swp391.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.group1.swp.pizzario_swp391.entity.DiningTable;

@Repository
public interface TableRepository extends JpaRepository<DiningTable, Integer> {

    @Query("select dt from DiningTable dt where dt.tableCondition != 'RETIRED'")
    List<DiningTable> getAllTablesForCashier();

    @Query("select dt from DiningTable dt where dt.tableCondition != 'RETIRED'")
    List<DiningTable> getAllTablesForGuest();

    List<DiningTable> getDiningTableByTableCondition(DiningTable.TableCondition tableCondition);

    @Query("select dt from DiningTable dt where dt.tableCondition != 'RETIRED'")
    List<DiningTable> getDiningTableByTableConditionExceptRetired();

}

package com.IShnitko.Tr1Count_bot.repository;

import com.IShnitko.Tr1Count_bot.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findExpensesByGroupId(String groupId);

    Page<Expense> findByGroupIdOrderByDateDesc(String groupId, Pageable pageable);

    Optional<Expense> findExpenseById(long id);

}

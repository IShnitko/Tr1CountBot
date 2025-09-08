package com.IShnitko.Tr1Count_bot.repository;

import com.IShnitko.Tr1Count_bot.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findExpensesByGroupId(String groupId);
}

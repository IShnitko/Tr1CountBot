package com.IShnitko.Tr1Count_bot.repository;

import com.IShnitko.Tr1Count_bot.model.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {
    void deleteByExpenseId(Long expenseId);

    List<ExpenseShare> findByExpenseIdIn(List<Long> expenseIds);
}

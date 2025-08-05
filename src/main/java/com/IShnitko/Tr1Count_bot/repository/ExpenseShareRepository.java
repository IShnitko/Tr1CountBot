package com.IShnitko.Tr1Count_bot.repository;

import com.IShnitko.Tr1Count_bot.model.Expense;
import com.IShnitko.Tr1Count_bot.model.ExpenseShare;
import com.IShnitko.Tr1Count_bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {

    List<ExpenseShare> findExpenseSharesByUser(User user);
    @Query("SELECT es from ExpenseShare es where es.user = :user and es.expense = :expense")
    List<ExpenseShare> findExpenseSharesByUserAndByExpenseId(User user, Expense expense);

    List<ExpenseShare> findExpenseSharesByExpense(Expense expense);
}

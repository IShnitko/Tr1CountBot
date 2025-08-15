package com.IShnitko.Tr1Count_bot.repository;

import com.IShnitko.Tr1Count_bot.model.Expense;
import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("select e from Expense e where e.paidBy = :paidBy and e.group.id = :groupId")
    List<Expense> findExpensesByPaidByFromGroup(User paidBy, String groupId);

    List<Expense> findExpensesByGroupId(String groupId);
}

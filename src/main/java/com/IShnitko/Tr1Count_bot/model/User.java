package com.IShnitko.Tr1Count_bot.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "users", schema = "tricount_schema")
public class User {
    @Id
    @Column(name = "telegram_id")
    private Long telegramId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "username", length = 100)
    private String username;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<Group> createdGroups;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<GroupMembership> memberships;

    @OneToMany(mappedBy = "paidBy", fetch = FetchType.LAZY)
    private List<Expense> personalExpenses;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ExpenseShare> expenseShares;
}

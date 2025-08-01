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

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "createdBy",
            cascade = {CascadeType.DETACH, CascadeType.PERSIST, CascadeType.DETACH},
            fetch = FetchType.LAZY)
    private List<Group> createdGroups;

    @OneToMany(mappedBy = "user",
            cascade = {CascadeType.DETACH, CascadeType.PERSIST, CascadeType.DETACH},
            fetch = FetchType.LAZY)
    private List<GroupMembership> memberships;

    @OneToMany(mappedBy = "paidBy",
            cascade = {CascadeType.DETACH, CascadeType.PERSIST, CascadeType.DETACH},
            fetch = FetchType.LAZY)
    private List<Expense> personalExpenses;

    @OneToMany(mappedBy = "user",
            cascade = {CascadeType.DETACH, CascadeType.PERSIST, CascadeType.DETACH},
            fetch = FetchType.LAZY)
    private List<ExpenseShare> expenseShares;
}

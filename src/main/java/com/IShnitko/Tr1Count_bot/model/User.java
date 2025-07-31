package com.IShnitko.Tr1Count_bot.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

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

    @OneToMany(mappedBy = "users",
            cascade = {CascadeType.DETACH, CascadeType.PERSIST, CascadeType.DETACH},
            fetch = FetchType.LAZY)
    private List<ExpenseShare> expenseShares;
}

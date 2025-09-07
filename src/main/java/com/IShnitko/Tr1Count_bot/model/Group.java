package com.IShnitko.Tr1Count_bot.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "groups", schema = "tricount_schema")
public class Group {
    @Id
    @Column(name = "id", length = 10)
    private String id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<GroupMembership> members;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<Expense> groupExpenses;
}

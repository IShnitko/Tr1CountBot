package com.IShnitko.Tr1Count_bot.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "groups",
            cascade = {CascadeType.DETACH, CascadeType.PERSIST, CascadeType.DETACH},
            fetch = FetchType.LAZY)
    private List<GroupMembership> members;

    @OneToMany(mappedBy = "groups",
            cascade = {CascadeType.DETACH, CascadeType.PERSIST, CascadeType.DETACH},
            fetch = FetchType.LAZY)
    private List<Expense> groupExpenses;
}

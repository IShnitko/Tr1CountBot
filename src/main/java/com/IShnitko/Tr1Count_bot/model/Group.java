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
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @Column(name = "invitation_code", unique = true, nullable = false)
    private String invitationCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "group",
            cascade = {CascadeType.DETACH, CascadeType.PERSIST, CascadeType.DETACH},
            fetch = FetchType.LAZY)
    private List<GroupMembership> members;

    @OneToMany(mappedBy = "group",
            cascade = {CascadeType.DETACH, CascadeType.PERSIST, CascadeType.DETACH},
            fetch = FetchType.LAZY)
    private List<Expense> groupExpenses;
}

package com.IShnitko.Tr1Count_bot.repository;

import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Group findGroupById(long id);

    Optional<Group> findGroupByName(String name);

    List<Group> findGroupByCreatedBy(User createdBy);

    @Query("SELECT g FROM Group g WHERE g.createdBy.telegramId = :userId")
    List<Group> findGroupsByCreatedByUserId(@Param("userId") Long userId);
}

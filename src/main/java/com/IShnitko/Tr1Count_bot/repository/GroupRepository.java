package com.IShnitko.Tr1Count_bot.repository;

import com.IShnitko.Tr1Count_bot.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findGroupById(String id);

    void deleteGroupById(String id);

    @Query("SELECT u.telegramId from User u join Group g on g.createdBy = u where g.id = :groupId")
    Optional<Long> findCreatorIdByGroupId(String groupId);

    boolean existsGroupById(String id);
}

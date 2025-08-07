package com.IShnitko.Tr1Count_bot.repository;

import com.IShnitko.Tr1Count_bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramId(Long telegramId);

    User findUserByTelegramId(Long telegramId);

    @Query("SELECT u " +
            "FROM User u " +
            "JOIN Group g on g.createdBy = u " +
            "WHERE g.id = :groupId")
    User findCreatorOfGroup(String groupId);

    @Query("SELECT u " +
            "from User u " +
            "join GroupMembership gm on gm.user = u " +
            "where gm.group.id = :groupId")
    List<User> findUsersByGroup(String groupId);

    User findUsersByTelegramId(Long telegramId);
}

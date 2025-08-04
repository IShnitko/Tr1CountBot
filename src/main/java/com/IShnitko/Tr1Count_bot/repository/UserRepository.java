package com.IShnitko.Tr1Count_bot.repository;

import com.IShnitko.Tr1Count_bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramId(Long telegramId);

    User findUserByTelegramId(Long telegramId);

    @Query("SELECT u " +
            "FROM User u " +
            "JOIN Group g on g.createdBy = u " +
            "WHERE g.id = :groupId")
    User findCreatorOfGroup(Long groupId);
}

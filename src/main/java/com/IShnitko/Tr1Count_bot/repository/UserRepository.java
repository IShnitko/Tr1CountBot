package com.IShnitko.Tr1Count_bot.repository;

import com.IShnitko.Tr1Count_bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramId(Long telegramId);

    Optional<User> findUserByTelegramId(Long telegramId);

    @Query("SELECT u " +
            "from User u " +
            "join GroupMembership gm on gm.user = u " +
            "where gm.group.id = :groupId")
    List<User> findUsersByGroup(String groupId);

    List<User> findAllByTelegramIdIn(Set<Long> longs);

    @Query("SELECT u from User u join Group g on g.createdBy = u where g.id = :groupId")
    Optional<User> findCreatorOfTheGroup(String groupId);

    boolean existsByTelegramIdIn(List<Long> esIds);
}

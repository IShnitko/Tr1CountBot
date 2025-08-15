package com.IShnitko.Tr1Count_bot.repository;

import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.GroupMembership;
import com.IShnitko.Tr1Count_bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {

    @Query("SELECT gm.group FROM GroupMembership gm WHERE gm.user.telegramId = :userId")
    List<Group> findGroupsByUserId(Long userId);

    void deleteGroupMembershipByUser(User user);

    @Query("SELECT gm.user FROM GroupMembership gm WHERE gm.group.id = :groupId")
    List<User> findUsersByGroupId(String groupId);

    @Query("SELECT g.createdBy.telegramId FROM Group g WHERE g.id = :groupId")
    Optional<Long> findCreatorIdByGroupId(String groupId);

    @Query("SELECT g.name FROM Group g WHERE g.id = :groupId")
    Optional<String> findGroupNameById(String groupId);

    boolean existsByGroupIdAndUser_TelegramId(String groupId, Long userTelegramId);

    int deleteByGroupIdAndUser_TelegramId(String groupId, Long userTelegramId);
}

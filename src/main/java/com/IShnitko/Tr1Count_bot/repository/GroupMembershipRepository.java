package com.IShnitko.Tr1Count_bot.repository;

import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.GroupMembership;
import com.IShnitko.Tr1Count_bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {

    @Query("SELECT gm.group FROM GroupMembership gm WHERE gm.user = :user")
    List<Group> findGroupsByUser(User user);

    void deleteGroupMembershipByUser(User user);

    @Query("SELECT gm.user FROM GroupMembership gm WHERE gm.group = :group")
    List<User> findUsersByGroup(Group group);
}

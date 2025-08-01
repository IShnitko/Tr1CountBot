package com.IShnitko.Tr1Count_bot.repository;

import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.GroupMembership;
import com.IShnitko.Tr1Count_bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {

    List<Group> findGroupMembershipsByUser(User user);
}

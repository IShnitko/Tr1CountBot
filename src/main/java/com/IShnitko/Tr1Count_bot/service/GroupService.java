package com.IShnitko.Tr1Count_bot.service;

import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.GroupMembership;
import com.IShnitko.Tr1Count_bot.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

public interface GroupService {
    Group createGroup(String name, User creator);
    GroupMembership addUserToGroup(Long groupId, Long userId);
    List<Group> getGroupsForUser(Long userId);
    void deleteUserFromGroup(Long groupId, Long userId);
    List<User> getUsersForGroup(Long groupId);
    Group updateGroupName(Long groupId, String newName);
    void deleteGroup(Long groupId);
}

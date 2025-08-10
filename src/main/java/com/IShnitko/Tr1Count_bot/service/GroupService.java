package com.IShnitko.Tr1Count_bot.service;

import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.GroupMembership;
import com.IShnitko.Tr1Count_bot.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

public interface GroupService {
    Group createGroup(String name, User creator);
    GroupMembership addUserToGroup(String groupId, Long userId);
    List<Group> getGroupsForUser(Long userId);
    void deleteUserFromGroup(String groupId, Long userId);
    List<User> getUsersForGroup(String groupId);
    Group updateGroupName(String groupId, String newName);
    void deleteGroup(String groupId);
    String getGroupName(String groupId); // TODO: Maybe change to return group info or just group object
}

package com.IShnitko.Tr1Count_bot.service.impl;

import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.GroupMembership;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.repository.GroupMembershipRepository;
import com.IShnitko.Tr1Count_bot.repository.GroupRepository;
import com.IShnitko.Tr1Count_bot.repository.UserRepository;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    @Autowired
    public GroupServiceImpl(GroupRepository groupRepository, UserRepository userRepository, GroupMembershipRepository groupMembershipRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.groupMembershipRepository = groupMembershipRepository;
    }

    @Override
    public Group createGroup(String name, User creator) {
        Group group = new Group();
        group.setName(name);
        group.setCreatedAt(LocalDateTime.now());
        group.setCreatedBy(creator);
        return groupRepository.save(group);
    }

    @Override
    public GroupMembership addUserToGroup(Long groupId, Long userId) {
        Group group = groupRepository.findGroupById(groupId);
        User user = userRepository.findUserByTelegramId(userId);

        GroupMembership gm = new GroupMembership();
        gm.setGroup(group);
        gm.setUser(user);
        gm.setJoinedAt(LocalDateTime.now());

        var potentialGMByGroup = groupMembershipRepository.findGroupMembershipsByGroup(group);
        if (potentialGMByGroup.contains(user)) return null;

        return groupMembershipRepository.save(gm);
    }

    @Override
    public List<Group> getGroupsForUser(Long userId) {
        return  groupMembershipRepository.findGroupMembershipsByUser(
                userRepository.findUserByTelegramId(userId));
    }

    @Override
    public void deleteUserFromGroup(Long groupId, Long userId) {
        User user = userRepository.findCreatorOfGroup(groupId);
        if (!user.getTelegramId().equals(userId)) throw new RuntimeException("Can't delete creator of the group");
        user = userRepository.findUserByTelegramId(userId);
        if (user == null) throw new RuntimeException("User doesn't exist in this group");
        groupMembershipRepository.deleteGroupMembershipByUser(user);
    }

    @Override
    public List<User> getUsersForGroup(Long groupId) {
        return groupMembershipRepository.findGroupMembershipsByGroup(groupRepository.findGroupById(groupId));
    }

    @Override
    public Group updateGroupName(Long groupId, String newName) {
        Group group = groupRepository.findGroupById(groupId);
        group.setName(newName);
        return groupRepository.save(group);
    }

    @Override
    public void deleteGroup(Long groupId) {
        groupRepository.deleteGroupById(groupId);
    }
}

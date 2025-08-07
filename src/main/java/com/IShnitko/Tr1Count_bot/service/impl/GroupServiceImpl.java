package com.IShnitko.Tr1Count_bot.service.impl;

import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.GroupMembership;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.repository.GroupMembershipRepository;
import com.IShnitko.Tr1Count_bot.repository.GroupRepository;
import com.IShnitko.Tr1Count_bot.repository.UserRepository;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.util.GroupCodeGenerator;
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
        while (true) {
            String code = GroupCodeGenerator.generateCode(5);
            if (groupRepository.findGroupById(code) == null) {
                group.setId(code);
                break;
            }
        }
        group.setName(name);
        group.setCreatedAt(LocalDateTime.now());
        group.setCreatedBy(creator);
        return groupRepository.save(group);
    }

    @Override
    public GroupMembership addUserToGroup(String groupId, Long userId) {
        Group group = groupRepository.findGroupById(groupId);
        User user = userRepository.findUserByTelegramId(userId);

        GroupMembership gm = new GroupMembership();
        gm.setGroup(group);
        gm.setUser(user);
        gm.setJoinedAt(LocalDateTime.now());

        var potentialGMByGroup = groupMembershipRepository.findUsersByGroup(group);
        if (potentialGMByGroup.contains(user)) return null;

        return groupMembershipRepository.save(gm);
    }

    @Override
    public List<Group> getGroupsForUser(Long userId) {
        return  groupMembershipRepository.findGroupsByUser(
                userRepository.findUserByTelegramId(userId));
    }

    @Override
    public void deleteUserFromGroup(String groupId, Long userId) {
        User user = userRepository.findCreatorOfGroup(groupId);
        if (!user.getTelegramId().equals(userId)) throw new RuntimeException("Can't delete creator of the group");
        user = userRepository.findUserByTelegramId(userId);
        if (user == null) throw new RuntimeException("User doesn't exist in this group");
        groupMembershipRepository.deleteGroupMembershipByUser(user);
    }

    @Override
    public List<User> getUsersForGroup(String groupId) {
        return groupMembershipRepository.findUsersByGroup(groupRepository.findGroupById(groupId));
    }

    @Override
    public Group updateGroupName(String groupId, String newName) {
        Group group = groupRepository.findGroupById(groupId);
        group.setName(newName);
        return groupRepository.save(group);
    }

    @Override
    public void deleteGroup(String groupId) {
        groupRepository.deleteGroupById(groupId);
    }
}

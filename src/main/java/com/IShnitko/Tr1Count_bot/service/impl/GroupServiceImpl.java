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
        GroupMembership gm = new GroupMembership();
        gm.setGroup(groupRepository.findGroupById(groupId));
        gm.setUser(userRepository.findUserByTelegramId(userId));
        gm.setJoinedAt(LocalDateTime.now());
        return groupMembershipRepository.save(gm);
    }

    @Override
    public List<Group> getGroupsForUser(Long userId) {
        return  groupMembershipRepository.findGroupMembershipsByUser(
                userRepository.findUserByTelegramId(userId));
    }
}

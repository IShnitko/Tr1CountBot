package com.IShnitko.Tr1Count_bot.service.impl;

import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.GroupMembership;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.repository.GroupMembershipRepository;
import com.IShnitko.Tr1Count_bot.repository.GroupRepository;
import com.IShnitko.Tr1Count_bot.repository.UserRepository;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.util.GroupCodeGenerator;
import com.IShnitko.Tr1Count_bot.util.exception.CreatorDeletionException;
import com.IShnitko.Tr1Count_bot.util.exception.GroupNotFoundException;
import com.IShnitko.Tr1Count_bot.util.exception.UserAlreadyInGroupException;
import com.IShnitko.Tr1Count_bot.util.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
        String invitationCode;
        do {
            invitationCode = GroupCodeGenerator.generateCode(5);
        } while (groupRepository.findByInvitationCode(invitationCode).isPresent());
        group.setInvitationCode(invitationCode);
        group.setName(name);
        group.setCreatedAt(LocalDateTime.now());
        group.setCreatedBy(creator);
        addUserToGroup(group.getId(), creator.getTelegramId());
        return groupRepository.save(group);
    }

    @Override
    public GroupMembership addUserToGroup(String groupId, Long userId) {
        Group group = groupRepository.findGroupById(groupId);
        if (groupRepository.findGroupById(groupId) == null) throw new GroupNotFoundException("Can't add user to group, because group "+ groupId +" doesn't exist");
        User user = userRepository.findUserByTelegramId(userId);
        if (userRepository.findUserByTelegramId(userId) == null) throw new UserNotFoundException("Can't add user to group, because user " + userId + " doesn't exist");

        GroupMembership gm = new GroupMembership();
        gm.setGroup(group);
        gm.setUser(user);
        gm.setJoinedAt(LocalDateTime.now());

        var potentialGMByGroup = groupMembershipRepository.findUsersByGroup(group);
        if (potentialGMByGroup.contains(user)) return null;

        return groupMembershipRepository.save(gm);
    }

    @Override
    public GroupMembership joinGroupByInvitation(String invitationCode, Long userId) {

        Optional<Group> optionalGroup = groupRepository.findByInvitationCode(invitationCode);

        if (optionalGroup.isEmpty()) {
            throw new GroupNotFoundException("Group with code '" + invitationCode + "' not found.");
        }

        Group group = optionalGroup.get();

        User user = userRepository.findUserByTelegramId(userId);

        if (user == null) {
            throw new UserNotFoundException("User with ID '" + userId + "' not found.");
        }

        if (groupMembershipRepository.findUsersByGroup(group).contains(user)) {
            throw new UserAlreadyInGroupException("User "+ userId +" is already in group " + group.getId());
        }

        GroupMembership membership = new GroupMembership();
        membership.setGroup(group);
        membership.setUser(user);
        membership.setJoinedAt(LocalDateTime.now());

        return groupMembershipRepository.save(membership);
    }

    @Override
    public List<Group> getGroupsForUser(Long userId) {
        if (userRepository.findUserByTelegramId(userId) == null) throw new UserNotFoundException("Can't get groups for user, because user " + userId + " doesn't exist");
        return  groupMembershipRepository.findGroupsByUser(
                userRepository.findUserByTelegramId(userId));
    }

    @Override
    public void deleteUserFromGroup(String groupId, Long userId) {
        User user = userRepository.findCreatorOfGroup(groupId);
        if (!user.getTelegramId().equals(userId)) throw new CreatorDeletionException("Can't delete creator of the group");
        user = userRepository.findUserByTelegramId(userId);
        if (user == null) throw new UserNotFoundException("User "+ userId +" doesn't exist in this group");
        groupMembershipRepository.deleteGroupMembershipByUser(user);
    }

    @Override
    public List<User> getUsersForGroup(String groupId) {
        if (groupRepository.findGroupById(groupId) == null) throw new GroupNotFoundException("Can't get members of the group, because group "+ groupId +" doesn't exist");
        return groupMembershipRepository.findUsersByGroup(groupRepository.findGroupById(groupId));
    }

    @Override
    public Group updateGroupName(String groupId, String newName) {
        Group group = groupRepository.findGroupById(groupId);
        if (group == null) throw new GroupNotFoundException("Can't update group name, because group "+ groupId +" doesn't exist");
        group.setName(newName);
        return groupRepository.save(group);
    }

    @Override
    public void deleteGroup(String groupId) {
        if (groupRepository.findGroupById(groupId) == null) throw new GroupNotFoundException("Group "+ groupId +" that is being deleted doesn't exist");
        groupRepository.deleteGroupById(groupId);
    }

    @Override
    public String getGroupName(String groupId) {
        if (groupRepository.findGroupById(groupId) == null) throw new GroupNotFoundException("Can't get group name, because group "+ groupId +" doesn't exist");
        return groupRepository.findGroupById(groupId).getName();
    }
}

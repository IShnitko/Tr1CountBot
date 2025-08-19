package com.IShnitko.Tr1Count_bot.service.impl;

import com.IShnitko.Tr1Count_bot.model.*;
import com.IShnitko.Tr1Count_bot.repository.*;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.util.GroupCodeGenerator;
import com.IShnitko.Tr1Count_bot.util.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    private static final int MAX_CODE_GENERATION_ATTEMPTS = 10;

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository expenseShareRepository;

    @Autowired
    public GroupServiceImpl(GroupRepository groupRepository,
                            UserRepository userRepository,
                            GroupMembershipRepository groupMembershipRepository, ExpenseRepository expenseRepository, ExpenseShareRepository expenseShareRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.expenseRepository = expenseRepository;
        this.expenseShareRepository = expenseShareRepository;
    }

    @Override
    @Transactional
    public Group createGroup(String name, Long userId) {
        User creator = userRepository.findUserByTelegramId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        String code = generateUniqueGroupCode();

        Group group = new Group();
        group.setId(code);
        group.setName(name);
        group.setCreatedAt(LocalDateTime.now());
        group.setCreatedBy(creator);

        try {
            return groupRepository.save(group);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Failed to create group after generating unique code", e);
        }
    }

    private String generateUniqueGroupCode() {
        for (int i = 0; i < MAX_CODE_GENERATION_ATTEMPTS; i++) {
            String code = GroupCodeGenerator.generateCode(10);
            if (groupRepository.findGroupById(code).isEmpty()) {
                return code;
            }
        }
        throw new IllegalStateException("Failed to generate unique group code after " + MAX_CODE_GENERATION_ATTEMPTS + " attempts");
    }

    @Override
    @Transactional
    public GroupMembership joinGroupById(String groupId, Long userId) {

        if (groupMembershipRepository.existsByGroupIdAndUser_TelegramId(groupId, userId)) {
            throw new UserAlreadyInGroupException("User " + userId + " is already in group " + groupId);
        }

        GroupMembership membership = new GroupMembership();
        membership.setGroup(groupRepository.findGroupById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Can't create GroupMembership, because group %s doesn't exist".formatted(groupId)))); // Proxy entity
        membership.setUser(userRepository.findUserByTelegramId(userId)
                .orElseThrow(() -> new UserNotFoundException("Can't create GroupMembership, because user %s doesn't exist".formatted(userId))));    // Proxy entity
        membership.setJoinedAt(LocalDateTime.now());

        return groupMembershipRepository.save(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getGroupsForUser(Long userId) {
        return groupMembershipRepository.findGroupsByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteUserFromGroup(String groupId, Long userId) {
        // Проверяем, является ли пользователь создателем группы
        Long creatorId = groupRepository.findCreatorIdByGroupId(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));

        if (creatorId.equals(userId)) {
            throw new CreatorDeletionException("Cannot delete creator from group");
        }
        List<Expense> expenses = expenseRepository.findExpensesByGroupId(groupId);
        List<Long> expenseIds = expenses.stream().map(Expense::getId).collect(Collectors.toList());

        List<ExpenseShare> allShares = expenseShareRepository.findByExpenseIdIn(expenseIds);
        List<Long> esIds = allShares.stream().map(e -> e.getUser().getTelegramId()).toList();

// Corrected logic: Use a method that returns a boolean
        boolean userHasHistory = userRepository.existsByTelegramIdIn(esIds);

        if (userHasHistory) {
            throw new NotEmptyHistoryException("User %s has a history of expenses and can't be deleted".formatted(userId));
        }

        int deleted = groupMembershipRepository.deleteByGroupIdAndUser_TelegramId(groupId, userId);
        if (deleted == 0) {
            throw new UserNotFoundException("User not found in group: " + userId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersForGroup(String groupId) {
        groupRepository.findGroupById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Can't get users for group %s, because it doesn't exits".formatted(groupId)));
        return groupMembershipRepository.findUsersByGroupId(groupId);
    }

    @Override
    @Transactional
    public Group updateGroupName(String groupId, String newName) {
        Group group = groupRepository.findGroupById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));

        group.setName(newName);
        return groupRepository.save(group);
    }

    @Override
    @Transactional
    public void deleteGroup(String groupId) {
        groupRepository.findGroupById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Can't delete group %s, because it doesn't exits".formatted(groupId)));

        groupRepository.deleteGroupById(groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public String getGroupName(String groupId) {
        return groupRepository.findGroupById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId))
                .getName();
    }

    @Override
    public boolean doesGroupExist(String groupId) {
        return groupRepository.existsGroupById(groupId);
    }

    @Override
    @Transactional
    public boolean doesUserExistInGroup(Long userId, String groupId) {
        // Get the current user's Telegram ID from the state manager.
        // Find the group by its ID. Using Optional to handle cases where the group does not exist.
        Optional<Group> optionalGroup = groupRepository.findGroupById(groupId);

        // If the group is present, check if its list of users contains the current user's ID.
        return optionalGroup.map(group -> group.getMembers().stream()
                        // Use anyMatch to find if any user's Telegram ID matches the current user's ID.
                        // This is an efficient way to check for existence without iterating through all users.
                        .anyMatch(groupMembership -> groupMembership.getUser().getTelegramId().equals(userId)))
                .orElse(false);
    }

}
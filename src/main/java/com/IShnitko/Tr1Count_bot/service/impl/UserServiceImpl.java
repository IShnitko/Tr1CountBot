package com.IShnitko.Tr1Count_bot.service.impl;

import com.IShnitko.Tr1Count_bot.model.GroupMembership;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.repository.GroupMembershipRepository;
import com.IShnitko.Tr1Count_bot.repository.UserRepository;
import com.IShnitko.Tr1Count_bot.service.UserService;
import com.IShnitko.Tr1Count_bot.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, GroupMembershipRepository groupMembershipRepository) {
        this.userRepository = userRepository;
        this.groupMembershipRepository = groupMembershipRepository;
    }

    /**
     * Finds an existing user by their Telegram ID or creates a new one.
     * If the user exists, the method updates their name and username if they have changed.
     *
     * @param telegramUser The user object from Telegram.
     * @return The found or created user object.
     */
    public User findOrCreateUser(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        Long telegramId = telegramUser.getId();
        Optional<User> existingUser = userRepository.findUserByTelegramId(telegramId);

        // If the user already exists, update their data if it has changed.
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            boolean needsUpdate = false;

            // Compare name
            if (!Objects.equals(user.getName(), telegramUser.getFirstName())) {
                user.setName(telegramUser.getFirstName());
                needsUpdate = true;
            }

            // Compare username. It's important to use Objects.equals for comparison,
            // to avoid a NullPointerException if one of the usernames is null.
            if (!Objects.equals(user.getUsername(), telegramUser.getUserName())) {
                user.setUsername(telegramUser.getUserName());
                needsUpdate = true;
            }

            if (needsUpdate) {
                userRepository.save(user);
                LOG.info("User with Telegram ID {} has been updated with new name '{}' and username '{}'.",
                        telegramId, user.getName(), user.getUsername());
            }

            return user;
        } else {
            // If the user doesn't exist, create a new one.
            User newUser = new User();
            newUser.setTelegramId(telegramId);
            newUser.setName(telegramUser.getFirstName());
            newUser.setUsername(telegramUser.getUserName());
            User savedUser = userRepository.save(newUser);
            LOG.info("New user with Telegram ID {}, username {} and name '{}' has been created.",
                    telegramId, savedUser.getUsername(), savedUser.getName());
            return savedUser;
        }
    }
    @Override
    public String getUserInfoForGroup(Long telegramId, String groupId) {
        // Formatter for a clean date display
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        // Get the user by their telegramId
        Optional<User> userOptional = userRepository.findUserByTelegramId(telegramId);

        // Get the user's group membership information
        List<GroupMembership> membershipList = groupMembershipRepository.findGroupMembershipByUser_TelegramIdAndGroup_Id(telegramId, groupId);

        // Check if both the user and their group membership exist
        if (userOptional.isEmpty() || membershipList.isEmpty()) {
            return "Failed to find information about the user or their group membership.";
        }

        User user = userOptional.get();
        GroupMembership membership = membershipList.getFirst();

        // Check if a username exists to create a proper link
        String userDisplayName;

        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            // If a username exists, use it as a display name
            userDisplayName = "@" + user.getUsername();
        } else {
            // If there's no username, use the user's name
            userDisplayName = user.getName();
        }

        // Return a plain text string without any Markdown
        return String.format(
                "User Information:\n" +
                        "Name: %s\n" +
                        "Joined: %s\n" +
                        "Contact: %s",
                user.getName(),
                membership.getJoinedAt().format(formatter),
                userDisplayName
        );
    }

    @Override
    public String getUserNameById(Long telegramId) {
        return userRepository.findUserByTelegramId(telegramId)
                .orElseThrow(() -> new UserNotFoundException("Can't get name for user %s, becuase it doesn't exitst".formatted(telegramId)))
                .getName();
    }

    @Override
    public Long getCreatorOfTheGroup(String groupId) {
        return userRepository.findCreatorOfTheGroup(groupId)
                .orElseThrow(() -> new UserNotFoundException("Can't find creator of the group " + groupId))
                .getTelegramId();
    }

    private String escapeMarkdownV2(String text) { // maybe move to utils
        if (text == null) {
            return "";
        }
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }
}

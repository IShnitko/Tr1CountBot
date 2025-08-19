package com.IShnitko.Tr1Count_bot.service.impl;

import com.IShnitko.Tr1Count_bot.model.GroupMembership;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.repository.GroupMembershipRepository;
import com.IShnitko.Tr1Count_bot.repository.UserRepository;
import com.IShnitko.Tr1Count_bot.service.UserService;
import com.IShnitko.Tr1Count_bot.util.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
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

    @Override
    public void findOrCreateUser(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        Long telegramId = telegramUser.getId();
        Optional<User> existingUser = userRepository.findUserByTelegramId(telegramId);

        if (existingUser.isEmpty()) {
            User newUser = new User();
            newUser.setTelegramId(telegramId);
            newUser.setName(telegramUser.getFirstName());
            userRepository.save(newUser);
            LOG.info("New user with Telegram ID {} and name '{}' has been created.", telegramId, telegramUser.getFirstName());
        }
    }

    @Override
    public String getUserInfoForGroup(Long telegramId, String groupId) {
        // Форматтер для красивого отображения даты
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        // Получаем пользователя по его telegramId
        Optional<User> userOptional = userRepository.findByTelegramId(telegramId);

        // Получаем информацию о членстве пользователя в конкретной группе
        List<GroupMembership> membershipOptional = groupMembershipRepository.findGroupMembershipByUser_TelegramIdAndGroup_Id(telegramId, groupId);

        // Проверяем, что и пользователь, и членство в группе существуют
        if (userOptional.isEmpty() || membershipOptional.isEmpty()) {
            return "Не удалось найти информацию о пользователе или его членстве в группе.";
        }

        User user = userOptional.get();
        GroupMembership membership = membershipOptional.getFirst();

        String userLink = String.format("tg://user?id=%d", telegramId);

        // Формируем итоговую строку с использованием Markdown-разметки
        // Важно: для MarkdownV2 необходимо экранировать специальные символы
        // в тексте, которые могут сломать разметку, например, в имени пользователя.
        String escapedName = escapeMarkdownV2(user.getName());

        return String.format(
                "*Info about user:*\n" +
                        "👤 *Name:* %s\n" +
                        "📅 *Joined:* %s\n" +
                        "🔗 [Contact user]( %s )", // TODO: idk why this link works only on mobile, idk how to get username here
                escapedName,
                membership.getJoinedAt().format(formatter),
                userLink
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

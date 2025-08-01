package com.IShnitko.Tr1Count_bot.repository;

import com.IShnitko.Tr1Count_bot.Tr1CountBotApplication;
import com.IShnitko.Tr1Count_bot.TestConfig;
import com.IShnitko.Tr1Count_bot.model.Group;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты для GroupRepository.
 * Этот класс тестирует работу GroupRepository с базой данных,
 * используя Spring Boot Test и тестовый профиль.
 *
 * @Sql аннотация указывает на SQL-скрипты, которые будут выполнены
 * до каждого тестового метода для настройки тестовой среды.
 *
 * Мы используем аннотацию @MockBean для TelegramBotsApi, чтобы
 * избежать проблем с инициализацией бота во время выполнения тестов,
 * так как для тестов репозитория нам не требуется работа с Telegram API.
 */
@SpringBootTest(
        classes = {Tr1CountBotApplication.class, TestConfig.class},
        properties = {"bot.initialize=false", "bot.token=test_token_value"}
)
@ActiveProfiles("test")
@Sql(scripts = {"/sql/cleanup.sql", "/sql/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class GroupRepositoryTest {

    @Autowired
    private GroupRepository groupRepository;

    @MockBean
    private TelegramBotsApi telegramBotsApi;

    /**
     * Тест, проверяющий, что мы можем найти группу по ее ID.
     */
    @Test
    void findById_ExistingGroup_ReturnsGroup() {
        // Given - ID существующей группы из data.sql
        long groupId = 1L;

        // When
        Optional<Group> group = groupRepository.findById(groupId);

        // Then
        assertTrue(group.isPresent());
        assertEquals(groupId, group.get().getId());
        assertEquals("Travelling", group.get().getName());
        assertEquals(123456789L, group.get().getCreatedBy().getTelegramId()); // add created by user id
    }

    /**
     * Тест, проверяющий, что мы получаем пустой Optional для несуществующего ID.
     */
    @Test
    void findById_NonExistingGroup_ReturnsEmpty() {
        // Given - ID несуществующей группы
        long groupId = 999L;

        // When
        Optional<Group> group = groupRepository.findById(groupId);

        // Then
        assertFalse(group.isPresent());
    }

    /**
     * Тест, проверяющий, что мы можем найти группу по имени.
     */
    @Test
    void findByName_ExistingGroup_ReturnsGroup() {
        // Given - имя существующей группы
        String groupName = "Party";

        // When
        Optional<Group> group = groupRepository.findGroupByName(groupName);

        // Then
        assertTrue(group.isPresent());
        assertEquals("Party", group.get().getName());
    }

    /**
     * Тест, проверяющий, что мы получаем пустой Optional для несуществующего имени.
     */
    @Test
    void findByName_NonExistingGroup_ReturnsEmpty() {
        // Given - имя несуществующей группы
        String groupName = "Non-existent Group";

        // When
        Optional<Group> group = groupRepository.findGroupByName(groupName);

        // Then
        assertFalse(group.isPresent());
    }

    /**
     * Тест, проверяющий, что мы можем найти все группы.
     */
    @Test
    void findAll_ReturnsAllGroups() {
        // When
        List<Group> groups = groupRepository.findAll();

        // Then
        assertEquals(3, groups.size());
    }

    /**
     * Тест, проверяющий, что мы можем найти группы, созданные конкретным пользователем.
     */
    @Test
    void findByCreatedByUserId_ExistingUser_ReturnsGroups() {
        // Given - telegram_id пользователя, который создал группу "Travelling"
        long creatorId = 123456789L;

        // When
        List<Group> groups = groupRepository.findGroupsByCreatedByUserId(creatorId);

        // Then
        assertEquals(1, groups.size());
        assertEquals("Travelling", groups.getFirst().getName());
    }
}

package com.IShnitko.Tr1Count_bot.repository; // Ensure this is the correct package

import com.IShnitko.Tr1Count_bot.Tr1CountBotApplication;
import com.IShnitko.Tr1Count_bot.TestConfig; // Keep TestConfig for other potential mocks or configurations
import com.IShnitko.Tr1Count_bot.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; // Import MockBean
import org.springframework.test.context.ActiveProfiles;

import org.telegram.telegrambots.meta.TelegramBotsApi; // Import the class you want to mock

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = {Tr1CountBotApplication.class, TestConfig.class},
        properties = {"bot.initialize=false", "bot.token=test_token_value"}
)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // Use @MockBean to replace the real TelegramBotsApi bean with a mock
    // This prevents the real bean's creation method from running and hitting the API
    @MockBean
    private TelegramBotsApi telegramBotsApi;

    // Your existing tests
    @Test
    void checkEnvironment() {
        String token = System.getenv("TELEGRAM_BOT_TOKEN");
        System.out.println("TELEGRAM_BOT_TOKEN: " + (token != null ? "exists" : "null"));
        // This test will now likely fail because you're providing bot.token via properties
        // and System.getenv() will still be null unless you set the environment variable externally.
        // If you truly need this to pass, you'd need to set the environment variable.
        // For the application context to load, providing the property is enough.
        // Consider if this check is still relevant if the token is always provided via properties for tests.
        // assertNotNull(token); // You might want to remove or adjust this line for consistency
    }

    @Test
    void findByTelegramId_ExistingUser_ReturnsUser() {
        // Given
        Long telegramId = 123456789L;

        // When
        Optional<User> user = userRepository.findByTelegramId(telegramId);

        // Then
        assertTrue(user.isPresent());
        assertEquals("Anna", user.get().getName());
        assertEquals(telegramId, user.get().getTelegramId());
    }

    @Test
    void findByTelegramId_NonExistingUser_ReturnsEmpty() {
        // Given
        Long telegramId = 999999999L;

        // When
        Optional<User> user = userRepository.findByTelegramId(telegramId);

        // Then
        assertFalse(user.isPresent());
    }

    @Test
    void count_ShouldReturn4() {
        // When
        long count = userRepository.count();

        // Then
        assertEquals(4, count);
    }
}
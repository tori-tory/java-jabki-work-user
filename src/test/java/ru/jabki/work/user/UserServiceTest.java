package ru.jabki.work.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.jabki.work.user.exception.NotFoundException;
import ru.jabki.work.user.exception.UserException;
import ru.jabki.work.user.model.User;
import ru.jabki.work.user.model.dto.ApiSuccess;
import ru.jabki.work.user.model.dto.UserRequest;
import ru.jabki.work.user.model.dto.UserResponse;
import ru.jabki.work.user.repository.UserRepository;
import ru.jabki.work.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void testCreateUser_valid() {
        UserRequest request = new UserRequest("username", "password");
        String passwordHash = "passwordHash";

        when(passwordEncoder.encode("password")).thenReturn(passwordHash);
        when(userRepository.insert(any(User.class))).thenReturn(1L);

        UserResponse response = userService.create(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("username", response.username());

        // Проверяем, что User был правильно передан в репозиторий
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).insert(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("username", savedUser.getUsername());
        assertEquals(passwordHash, savedUser.getPassword());

        // Проверяем, что passwordEncoder был вызван
        verify(passwordEncoder).encode("password");
        verify(userRepository, times(1)).insert(any(User.class));
    }

    @Test
    void testCreateUser_invalidData_nullUsername_throwUserException() {
        UserRequest request = new UserRequest("", "password");

        final UserException exception = assertThrows(
                UserException.class,
                () -> userService.create(request)
        );

        assertEquals("Имя пользователя не может быть пустым", exception.getMessage());
        verify(userRepository, never()).insert(any());
    }

    @Test
    void testCreateUser_invalidData_nullPassword_throwUserException() {
        UserRequest request = new UserRequest("username", "");

        final UserException exception = assertThrows(
                UserException.class,
                () -> userService.create(request)
        );

        assertEquals("Пароль пользователя не может быть пустым", exception.getMessage());
        verify(userRepository, never()).insert(any());
    }

    @Test
    void testExistsById_NoThrowException_WhenUserFound() {
        final User user = getUser();
        UserRequest request = new UserRequest("username", "password");
        when(userRepository.getById(1L)).thenReturn(user);

        UserResponse response = userService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("username", response.username());
        verify(userRepository, times(1)).getById(1L);
    }

    @Test
    void testExistsById_ThrowException_WhenUserNotFound() {
        final User user = getUser();
        Long userId = 99L;
        when(userRepository.getById(userId)).thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getById(userId)
        );

        assertEquals(String.format("Пользователь с id %d не найден", userId), exception.getMessage());
        verify(userRepository, times(1)).getById(userId);
    }

    @Test
    void testSoftDelete_success(){
        Long userId = 1L;
        when(userRepository.softDelete(userId)).thenReturn(1);

        ApiSuccess result = userService.softDelete(userId);

        assertTrue(result.success());
        verify(userRepository, times(1)).softDelete(userId);
    }

    @Test
    void testSoftDelete_throwNotFoundException(){
        Long userId = 99L;
        when(userRepository.softDelete(userId)).thenReturn(0);

        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.softDelete(userId)
        );

        assertEquals("Пользователь с id 99 не найден", exception.getMessage());
        verify(userRepository, times(1)).softDelete(userId);
}

    private User getUser() {
        return User.builder()
                .id(1L)
                .username("username")
                .password("password")
                .build();
    }
}
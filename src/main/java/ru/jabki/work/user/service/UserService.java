package ru.jabki.work.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.jabki.work.user.client.TaskClient;
import ru.jabki.work.user.exception.NotFoundException;
import ru.jabki.work.user.exception.UserException;
import ru.jabki.work.user.model.User;
import ru.jabki.work.user.model.dto.ApiSuccess;
import ru.jabki.work.user.model.dto.UserRequest;
import ru.jabki.work.user.model.dto.UserResponse;
import ru.jabki.work.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaskClient taskClient;

    @Transactional
    public UserResponse create(final UserRequest userRequest) {
        validate(userRequest);
        String passwordHash = passwordEncoder.encode(userRequest.password());
        User user = User.builder()
                .username(userRequest.username())
                .password(passwordHash)
                .role(userRequest.role())
                .build();

        return new UserResponse(userRepository.insert(user), userRequest.username(), userRequest.role());
    }

    @Transactional(readOnly = true)
    public UserResponse getById(final Long id) {
        User user = userRepository.getById(id);
        if (user == null) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден", id));
        }
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }

    @Transactional(readOnly = true)
    public boolean existsById(final Long id) {
        return userRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean isManager(final Long id) {
        return userRepository.isManager(id);
    }

    @Transactional
    public ApiSuccess softDelete(final Long id){
        if (taskClient.existsByAssigneeId(id)) {
            throw new UserException(String.format("Удалить пользователя с id %d нельзя. Есть активные задачи", id));
        }

        int rowsDeleted = userRepository.softDelete(id);

        if (rowsDeleted == 0) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден", id));
        }

        return new ApiSuccess(true);
    }

    @Transactional
    public List<UserResponse> getAll(){
        List<User> users = userRepository.getAll();

        return users.stream()
                .map(user -> new UserResponse(user.getId(), user.getUsername(), user.getRole()))
                .toList();
    }

    private void validate(final UserRequest userRequest) {
        if (!StringUtils.hasText(userRequest.username())) {
            throw new UserException("Имя пользователя не может быть пустым");
        }
        if (!StringUtils.hasText(userRequest.password())) {
            throw new UserException("Пароль пользователя не может быть пустым");
        }
    }
}
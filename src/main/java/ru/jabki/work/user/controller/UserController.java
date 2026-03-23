package ru.jabki.work.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.jabki.work.user.model.dto.ApiSuccess;
import ru.jabki.work.user.model.dto.UserRequest;
import ru.jabki.work.user.model.dto.UserResponse;
import ru.jabki.work.user.service.UserService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "Пользователи")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Создать пользователя")
    public UserResponse create(@RequestBody UserRequest userRequest) {
        return userService.create(userRequest);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по id")
    public UserResponse getById(@PathVariable("id") Long id) {
        return userService.getById(id);
    }

    @GetMapping("/exists/{id}")
    @Operation(summary = "Проверка, существует ли пользователь")
    public boolean existsById(@PathVariable("id") Long id) {
        return userService.existsById(id);
    }

    @GetMapping("/exists/manager/{id}")
    @Operation(summary = "Наличие у пользователя роли MANAGER")
    public boolean isManager(@PathVariable("id") Long id) {
        return userService.isManager(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя по id")
    public ApiSuccess delete(@PathVariable("id") Long id) {
        return userService.softDelete(id);
    }

    @GetMapping("/all")
    @Operation(summary = "Получить список пользователей")
    public ResponseEntity<List<UserResponse>> getAll() {
        List<UserResponse> users = userService.getAll();
        return ResponseEntity.ok(users);
    }
}
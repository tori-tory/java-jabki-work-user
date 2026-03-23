package ru.jabki.work.user.model.dto;

public record UserResponse(Long id, String username, UserRole role) {
}
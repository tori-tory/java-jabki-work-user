package ru.jabki.work.user.exception;

import lombok.Data;

@Data
public class ApiError {
    final boolean success;
    final String message;
}
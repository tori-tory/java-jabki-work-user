package ru.jabki.work.user.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TaskClient {
    private final RestClient restClient;

    public TaskClient(@Qualifier("restClientTask") RestClient restClient) {
        this.restClient = restClient;
    }

    public boolean existsByAssigneeId(Long userId) {
        return restClient.get()
                .uri("/api/v1/task/exists/assignee/{userId}", userId)
                .retrieve()
                .body(Boolean.class);
    }
}
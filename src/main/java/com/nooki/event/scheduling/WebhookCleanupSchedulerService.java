package com.nooki.event.scheduling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookCleanupSchedulerService {
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldWebhooks() {
        int deleted = jdbcTemplate.update("""
                DELETE FROM processed_webhook_events
                WHERE processed_at < NOW() - INTERVAL '180 days'
                """);

        log.info("Deleted {} old webhook events", deleted);
    }

}

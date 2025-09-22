package com.rasi.med.sync.local;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Profile("sede")
public class OutboxService {

    private final JdbcTemplate jdbc;

    @Transactional
    public void enqueue(String aggregateType, UUID aggregateId, String op, String jsonPayload){
        jdbc.update(
                "insert into outbox(id, aggregate_type, aggregate_id, op, payload) values (?,?,?, ?, cast(? as jsonb))",
                UUID.randomUUID(), aggregateType, aggregateId, op, jsonPayload
        );
    }

    public List<Map<String,Object>> pending(int limit){
        return jdbc.queryForList(
                "select id, aggregate_type, aggregate_id, op, payload::text as payload from outbox where published=false order by created_at asc limit ?",
                limit
        );
    }

    @Transactional
    public void markPublished(List<UUID> ids){
        if (ids.isEmpty()) return;
        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        jdbc.update("update outbox set published=true where id in ("+inSql+")", ids.toArray());
    }
}

package io.playground.chatservice.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEntity, Long> {
    List<OutboxEntity> findAllByProcessedOrderByOccurredAtAsc(boolean processed);

    @Modifying
    @Query("""
        update OutboxEntity e
            set e.processed = true
        where e.id in :ids
    """)
    void updateProcessedTrueByIds(List<Long> ids);
}

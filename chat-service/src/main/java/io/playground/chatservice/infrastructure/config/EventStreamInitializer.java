package io.playground.chatservice.infrastructure.config;

import io.playground.chatservice.application.read.handler.UserViewHandler;
import io.playground.chatservice.application.eventstream.EventGroupType;
import io.playground.chatservice.application.eventstream.EventStreamNamingStrategy;
import io.playground.chatservice.application.eventstream.EventStreamType;
import io.playground.chatservice.application.read.model.UserProfileCreatedEvent;
import io.playground.chatservice.domain.chat.message.ChatMessageSentEvent;
import io.playground.chatservice.infrastructure.messaging.consumer.RedisEventStreamListenerManager;
import io.playground.chatservice.infrastructure.pubsub.ChatMessageHandlerForPub;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventStreamInitializer {
    private final EventStreamNamingStrategy namingStrategy;
    private final RedisEventStreamListenerManager manager;
    private final UserViewHandler userViewHandler;
    private final ChatMessageHandlerForPub chatMessageHandlerForPub;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        String chatEventsStreamName = namingStrategy.toStreamName(EventStreamType.CHAT_EVENTS, null);
        String chatEventsGroupName = namingStrategy.toGroupName(chatEventsStreamName, EventGroupType.VIEW_UPDATER);

        manager.subscribe(
                chatEventsStreamName,
                chatEventsGroupName,
                "consumer-1",
                UserProfileCreatedEvent.class,
                userViewHandler
        );

        manager.subscribe(
                chatEventsStreamName,
                chatEventsGroupName,
                "consumer-2",
                UserProfileCreatedEvent.class,
                userViewHandler
        );

        chatEventsStreamName = namingStrategy.toStreamName(EventStreamType.CHAT_MESSAGES, null);

        manager.subscribe(
                chatEventsStreamName,
                namingStrategy.toGroupName(chatEventsStreamName, EventGroupType.MESSAGE_PUBLISHER),
                "only-consumer",
                ChatMessageSentEvent.class,
                chatMessageHandlerForPub
        );
    }
}

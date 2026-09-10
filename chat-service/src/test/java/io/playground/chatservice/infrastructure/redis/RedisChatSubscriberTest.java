package io.playground.chatservice.infrastructure.redis;

import io.playground.chatservice.application.event.ChatEventDto;
import io.playground.chatservice.domain.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisChatSubscriberTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private Message message;

    private RedisChatSubscriber sut;

    @BeforeEach
    void setUp() {
        sut = new RedisChatSubscriber(objectMapper, messagingTemplate);
    }

    @Test
    @DisplayName("채널로 수신한 이벤트를 역직렬화해 해당 채팅방 구독 경로로 그대로 브로드캐스트한다")
    void broadcastsDeserializedEventToRoomDestination() {
        ChatEventDto.ChatMessageSent dto = new ChatEventDto.ChatMessageSent(
                100L, 42L, 1L, ChatMessage.MessageType.TEXT, "hello", null, Instant.now()
        );
        when(message.getBody()).thenReturn(objectMapper.writeValueAsBytes(dto));

        sut.onMessage(message, "pattern".getBytes());

        ArgumentCaptor<ChatEventDto.ChatMessageSent> captor =
                ArgumentCaptor.forClass(ChatEventDto.ChatMessageSent.class);
        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/sub/room/42"), captor.capture());
        assertThat(captor.getValue().chatMessageId()).isEqualTo(100L);
        assertThat(captor.getValue().content()).isEqualTo("hello");
    }
}

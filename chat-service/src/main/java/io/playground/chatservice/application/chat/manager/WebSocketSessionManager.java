package io.playground.chatservice.application.chat.manager;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {
    private final Map<String, Set<String>> userDevices = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> userRooms = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> roomUsers = new ConcurrentHashMap<>();

    // ToDo: 그럼 유저의 방이 실시간으로 늘어나거나 줄면? 참가자는??
    public void addSession(String userId, String deviceId, List<Long> chatRoomIds) {
        userDevices
                .computeIfAbsent(userId, k -> new HashSet<>())
                .add(deviceId);

        userRooms.put(userId, Set.copyOf(chatRoomIds));

        for (Long roomId : chatRoomIds) {
            if (!roomUsers.containsKey(roomId))
                roomUsers.put(roomId, new HashSet<>());

            roomUsers.get(roomId).add(userId);
        }
    }

    public void removeSession(String userId, String deviceId) {
        userDevices.get(userId).remove(deviceId);

        if (!userDevices.get(userId).isEmpty())
            return;

        userDevices.remove(userId);
        for (Long roomId : userRooms.remove(userId))
            roomUsers.get(roomId).remove(userId);
    }
}

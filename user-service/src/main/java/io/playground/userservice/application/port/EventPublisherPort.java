package io.playground.userservice.application.port;

public interface EventPublisherPort {
    void publish(Object event);
}

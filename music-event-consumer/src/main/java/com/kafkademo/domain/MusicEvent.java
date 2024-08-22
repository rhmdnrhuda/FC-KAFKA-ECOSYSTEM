package com.kafkademo.domain;

public record MusicEvent(
    Integer musicEventId,
    MusicEventType musicEventType,
    Music music
) {
}

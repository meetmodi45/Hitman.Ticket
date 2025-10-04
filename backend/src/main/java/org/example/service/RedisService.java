package org.example.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Try to lock seat for selection/payment
    public boolean tryLockSeat(String seatNumber, Duration duration) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(seatNumber, "locked", duration);
        return success != null && success;
    }

    // Unlock seat manually
    public void unlockSeat(String seatNumber) {
        redisTemplate.delete(seatNumber);
    }

    // Check if seat is locked
    public boolean isSeatLocked(String seatNumber) {
        return redisTemplate.hasKey(seatNumber);
    }
}

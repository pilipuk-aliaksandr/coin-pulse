package by.pilipuk.coinpulse.service;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PriceValidator {

    public boolean isNotValidPeriod(Instant from, Instant to, Instant now) {
        return from == null || to == null || from.isAfter(to) || from.isAfter(now);
    }
}

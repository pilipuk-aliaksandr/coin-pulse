package by.pilipuk.coinpulse.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Нужна только цена; price приходит строкой и разбирается сразу в BigDecimal без double.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BinanceAvgPriceResponse(BigDecimal price) {
}

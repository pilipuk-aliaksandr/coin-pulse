package by.pilipuk.coinpulse.service;

import by.pilipuk.coinpulse.client.BinanceClient;
import by.pilipuk.coinpulse.mapper.PriceMapper;
import by.pilipuk.coinpulse.model.entity.Price;
import by.pilipuk.coinpulse.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceCollectorService {

    private final BinanceClient binanceClient;
    private final PriceRepository priceRepository;
    private final PriceMapper priceMapper;

    @Transactional
    public void collect(String symbol) {
        var averagePrice = binanceClient.fetchAveragePrice(symbol);

        var price = priceMapper.toEntity(symbol, averagePrice, LocalDateTime.now(ZoneOffset.UTC));

        Price saved = priceRepository.save(price);

        log.info(
                "Saved {} average price {} (fetchedAt={})",
                saved.getSymbol(),
                saved.getPrice(),
                saved.getFetchedAt()
        );
    }
}

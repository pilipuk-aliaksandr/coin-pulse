package by.pilipuk.coinpulse.repository;

import by.pilipuk.coinpulse.model.entity.Price;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PriceRepository extends JpaRepository<Price, Long> {

    Optional<Price> findFirstBySymbolOrderByFetchedAtDesc(String symbol);

    Optional<Price> findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtAsc(
            String symbol, LocalDateTime from, LocalDateTime to);

    Optional<Price> findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(
            String symbol, LocalDateTime from, LocalDateTime to);

    Page<Price> findBySymbolAndFetchedAtBetween(
            String symbol, LocalDateTime from, LocalDateTime to, Pageable pageable);

    @Query(value =
            """
                    SELECT AVG(p.price)
                    FROM coins.prices p
                    WHERE p.symbol = :symbol
                      AND p.fetched_at BETWEEN :fromTime AND :toTime
            """, nativeQuery = true)
    Optional<BigDecimal> findAveragePrice(@Param("symbol") String symbol,
                                          @Param("fromTime") LocalDateTime from,
                                          @Param("toTime") LocalDateTime to);
}

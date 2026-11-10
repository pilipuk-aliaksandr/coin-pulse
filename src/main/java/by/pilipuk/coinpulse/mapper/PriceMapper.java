package by.pilipuk.coinpulse.mapper;

import by.pilipuk.coinpulse.model.entity.PriceEntity;
import by.pilipuk.coinpulse.model.enums.Trend;
import by.pilipuk.dto.AveragePriceDto;
import by.pilipuk.dto.PriceDto;
import by.pilipuk.dto.PricePageResponseDto;
import by.pilipuk.dto.TrendResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface PriceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fetchedAt", source = "now")
    @Mapping(target = "createdAt", ignore = true)
    PriceEntity toEntity(String symbol, BigDecimal price, LocalDateTime now);

    @Mapping(target = "price", qualifiedByName = "toPlainString")
    PriceDto toPriceDto(PriceEntity priceEntity);

    @Mapping(target = "averagePrice", qualifiedByName = "toPlainString")
    AveragePriceDto toAveragePriceDto(BigDecimal averagePrice);

    default PricePageResponseDto toPageDto(Page<PriceEntity> page) {

        return PricePageResponseDto.builder()
                .items(page.getContent().stream().map(this::toPriceDto).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .hasNext(page.hasNext())
                .build();
    }

    @Mapping(target = "startDate", source = "from")
    @Mapping(target = "endDate", source = "to")
    TrendResponseDto toTrendDto(String symbol, Instant from, Instant to, Trend trend);

    default OffsetDateTime toOffsetDateTime(LocalDateTime utcDateTime) {
        return utcDateTime.atOffset(ZoneOffset.UTC);
    }

    default OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }

    @Named("toPlainString")
    default String toPlainString(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }
}

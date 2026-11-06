package by.pilipuk.coinpulse.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "prices", schema = "coins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "price", nullable = false, precision = 19, scale = 8)
    private BigDecimal price;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Price price1 = (Price) o;
        return Objects.equals(id, price1.id)
                && Objects.equals(symbol, price1.symbol)
                && Objects.equals(price, price1.price)
                && Objects.equals(fetchedAt, price1.fetchedAt)
                && Objects.equals(createdAt, price1.createdAt);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(symbol);
        result = 31 * result + Objects.hashCode(price);
        result = 31 * result + Objects.hashCode(fetchedAt);
        result = 31 * result + Objects.hashCode(createdAt);
        return result;
    }
}

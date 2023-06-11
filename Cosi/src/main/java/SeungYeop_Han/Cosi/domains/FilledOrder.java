package SeungYeop_Han.Cosi.domains;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Table(name = "filled_orders")
@Entity(name = "FilledOrder")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class FilledOrder {
    ///// 속성 /////
    @Id
    @Column(
            name = "order_id",
            updatable = false
    )
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "order_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "filled_orders_order_id_fk")
    )
    private Order order;

    @NonNull
    @CreatedDate
    @Column(
            name = "filled_datetime",
            columnDefinition = "DATETIME"
    )
    private LocalDateTime filledDatetime;

    @NonNull
    @Column(
            name = "filled_amount",
            columnDefinition = "DECIMAL(12, 8) CHECK(filled_amount >= 0.0)"
    )
    private Double filledAmount;

    @NonNull
    @Column(
            name = "filled_price",
            columnDefinition = "DECIMAL(14, 4) CHECK(filled_price >= 0.0)"
    )
    private Double filledPrice;

    @NonNull
    @Column(
            name = "unit_price",
            columnDefinition = "DECIMAL(14, 4) CHECK(unit_price >= 0.0)"
    )
    private Double unitPrice;

    @Column(
            name = "fee",
            columnDefinition = "DECIMAL(14, 4) CHECK(fee >= 0.0)"
    )
    private Double fee;
}

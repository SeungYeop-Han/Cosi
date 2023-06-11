package SeungYeop_Han.Cosi.domains;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Table(name = "orders")
@Entity(name = "Order")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    ///// 속성 /////
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @Column(
            name = "is_buying",
            columnDefinition = "BOOLEAN"
    )
    @NonNull
    private Boolean isBuying;

    @Column(name = "order_type")
    @Enumerated(EnumType.STRING)
    @NonNull
    private OrderType orderType;

    @Column(
            name = "order_datetime",
            columnDefinition = "DATETIME"
    )
    @CreatedDate
    @NonNull
    private LocalDateTime orderDatetime;

    @Column(name = "order_state")
    @Enumerated(EnumType.STRING)
    @NonNull
    private OrderState orderState;

    @Column(
            name = "order_amount",
            columnDefinition = "DECIMAL(12, 8) CHECK(order_amount >= 0.0)"
    )
    private Double orderAmount;

    @Column(
            name = "order_price",
            columnDefinition = "DECIMAL(14, 4) CHECK(order_price >= 0.0)"
    )
    private Double orderPrice;

    @Column(
            name = "stop_price",
            columnDefinition = "DECIMAL(14, 4) CHECK(stop_price >= 0.0)"
    )
    private Double stopPrice;

    @Column(
            name = "total_price",
            columnDefinition = "DECIMAL(16, 4) CHECK(total_price >= 5000)"
    )
    private Double totalPrice;

    ///// 참조 속성 /////
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "member_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "orders_member_id_fk")
    )
    @NonNull
    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "coin_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "orders_coin_id_fk")
    )
    @NonNull
    private Coin coin;

    ///// 역방향 참조 속성 /////
    @OneToOne(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private FilledOrder filledOrder;

    ///// 메소드 재정의: toString /////
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", isBuying=" + isBuying +
                ", orderType='" + orderType + '\'' +
                ", orderDatetime=" + orderDatetime +
                ", orderState='" + orderState + '\'' +
                ", orderAmount=" + orderAmount +
                ", orderPrice=" + orderPrice +
                ", stopPrice=" + stopPrice +
                ", totalPrice=" + totalPrice +
                ", member=" + member +
                ", coin=" + coin +
                '}';
    }
}

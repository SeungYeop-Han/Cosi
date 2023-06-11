package SeungYeop_Han.Cosi.domains;

import jakarta.persistence.*;
import lombok.*;


@Table(name = "wallets")
@Entity(name = "Wallet")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Wallet {
    ///// 속성 /////
    @EmbeddedId
    private WalletId walletId;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "member_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "wallets_member_id_fk")
    )
    private Member member;

    @MapsId("coinId")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "coin_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "wallets_coin_id_fk")
    )
    private Coin coin;

    @Column(
            name = "amount",
            columnDefinition = "DECIMAL(12, 8) CHECK(amount >= 0.0)"
    )
    @NonNull
    private Double amount;

    @Column(
            name = "avg_buy_price",
            columnDefinition = "DECIMAL(14, 4) CHECK(avg_buy_price >= 0.0)"
    )
    @NonNull
    private Double avgBuyPrice;

    @Column(
            name = "total_buy_price",
            columnDefinition = "DECIMAL(16, 4) CHECK(total_buy_price >= 0.0)"
    )
    @NonNull
    private Double totalBuyPrice;

    ///// 생성자 /////
    public Wallet(Member member,
                  Coin coin,
                  Double amount,
                  Double avgBuyPrice) {
        this.walletId = new WalletId(member.getId(), coin.getId());
        this.member = member;
        this.coin = coin;
        this.amount = amount;
        this.avgBuyPrice = avgBuyPrice;
        this.totalBuyPrice = (Double) this.amount * this.avgBuyPrice;
    }

    ///// 사용자 정의 메소드 /////
    /**
     * 지갑에 코인 구매를 반영합니다.
     * @param unitPrice Double: 코인 단가
     * @param buyAmount Double: 구매 개수
     * @throws RuntimeException 만약 구매할 코인의 양이 0보다 작거나 같은 경우 예외를 발생시킵니다.
     */
    public void takeIn(Double unitPrice, Double buyAmount) throws RuntimeException {
        if(buyAmount <= 0){
            throw new RuntimeException("획득한 코인의 양은 0보다 커야합니다.");
        }

        Double oldAmount = this.amount;
        Double oldAvgBuyPrice = this.avgBuyPrice;

        this.amount += buyAmount;
        this.avgBuyPrice = ((oldAmount * oldAvgBuyPrice) + (buyAmount * unitPrice)) / this.getAmount();
        this.totalBuyPrice = this.amount * this.avgBuyPrice;
    }

    /**
     * 지갑에 코인 판매를 반영합니다.
     * 만약 판매 개수가 0 이하이거나 현재 보유한 코인 개수보다 큰 경우 예외를 발생시킵니다.
     * @param sellAmount Double: 판매 개수
     */
    public void takeOut(Double sellAmount) {
        if(sellAmount <= 0){
            throw new RuntimeException("차감(판매)할 코인의 양은 양수여야 합니다");
        }

        if(sellAmount > this.amount){
            throw new RuntimeException("차감(판매)할 코인의 양은 보유하고 있는 코인의 양 보다 많을 수 없습니다.");
        }

        this.amount -= sellAmount;
        this.totalBuyPrice = this.amount * this.avgBuyPrice;
    }

    /**
     * 미체결된 판매건에 대해 취소한 경우 다시 코인의 양을 복구합니다.
     * 만약 복구할 코인의 수가 0 이하일 경우 예외를 발생시킵니다.
     * @param takenOutAmount Double: 주문에 묶여있던 코인 개수
     */
    public void cancelTakeOut(Double takenOutAmount){
        if(takenOutAmount <= 0) {
            throw new RuntimeException("복구할 코인의 양은 양수여야합니다.");
        }

        this.amount += takenOutAmount;
        this.totalBuyPrice = this.amount * this.avgBuyPrice;
    }
}

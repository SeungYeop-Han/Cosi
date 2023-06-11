package SeungYeop_Han.Cosi.repositories;

import SeungYeop_Han.Cosi.domains.Member;
import SeungYeop_Han.Cosi.domains.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.member.id = :memberId")
    public List<Order> findOrdersByMemberId(@Param("memberId") Long memberId);

    //SELECT (주문번호, 코인코드, 매수/매도, 주문수량, 주문가격, 주문총액, 감시가격, 주문시간)
    @Query("SELECT o.id, o.coin.code, o.isBuying, o.orderAmount, o.orderPrice, o.totalPrice, o.stopPrice, o.orderDatetime " +
            "FROM Order o WHERE o.member.id = :memberId AND o.orderState = 'UNFILLED'")
    public List<?> findUnfilledOrdersByMemberId(@Param("memberId") Long memberId);

    //SELECT (주문번호, 코인코드, 매수/매도, 주문유형, 주문단가, 체결단가, 체결수량, 체결금액, 주문총액, 주문시간, 체결시간, 수수료)
    @Query("SELECT o.id, o.coin.code, o.isBuying, o.orderType, o.orderPrice, o.filledOrder.unitPrice, o.filledOrder.filledAmount, o.filledOrder.filledPrice, o.totalPrice, o.orderDatetime, o.filledOrder.filledDatetime, o.filledOrder.fee " +
            "FROM Order o WHERE o.member.id = :memberId AND o.orderState = 'FILLED'")
    public List<?> findFilledOrdersByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT o.member FROM Order o WHERE o.id = :orderId")
    public Optional<Member> findMemberByOrderId(@Param("orderId") Long orderId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Order o WHERE o.member.id = :memberId")
    public void deleteOrdersByMemberId(@Param("memberId") Long memberId);
}

package SeungYeop_Han.Cosi.DTOs;

import SeungYeop_Han.Cosi.domains.OrderType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@JsonAutoDetect
@ToString
public class OrderSheet {

    //주문 유형 결정
    private Boolean isBuying;
    private OrderType orderType;

    //마켓 코드(ex. "KRW-BTC")
    private String coinCode;

    //감시가격
    private Double stopPrice;
    //주문가격
    private Double orderPrice;
    //주문수량
    private Double orderAmount;
    //주문총액
    private Double totalPrice;

}

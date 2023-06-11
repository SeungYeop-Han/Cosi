package SeungYeop_Han.Cosi.domains;

import lombok.*;

import java.util.HashMap;

@Getter
@Setter
@ToString
public class FeeRate {

    ///// 싱글턴 /////
    private static FeeRate feeRate;
    private FeeRate(){

        //수수료 테이블 정의
        feeRates.put(OrderType.LIMIT, 0.05);
        feeRates.put(OrderType.MARKET, 0.05);
        feeRates.put(OrderType.STOP_LIMIT, 0.139);

    }
    public static FeeRate getInstance(){
        if (feeRate == null) {
            feeRate = new FeeRate();
        }
        return feeRate;
    }

    ///// 속성 /////
    //HashMap<주문유형, 수수료율>
    private static final HashMap<OrderType, Double> feeRates = new HashMap<>();


    ///// 사용자 정의 메소드 /////

    /**
     * 주어진 주문유형(orderType)에 대응하는 수수료율을 반환합니다.
     * @param orderType OrderType: 주문 유형
     * @return 수수료율, 제시된 orderType이 잘못된 경우(아마도 null인 경우) null이 반환될 수 있습니다.
     */
    public Double getFeeRate(OrderType orderType) {
        return feeRates.get(orderType);
    }
}

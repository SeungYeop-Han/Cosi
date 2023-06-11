package SeungYeop_Han.Cosi.services;

import SeungYeop_Han.Cosi.domains.Order;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

//판매 주문 우선순위 큐
public class SellOrderQueue {

    ///// 속성 /////
    // 큐에 저장될 코인 종목
    private final String coinCode;

    //특정 코인 종목에 대한 주문(Order)들이 저장될 우선순위 큐
    private final PriorityBlockingQueue<Order> sellOrderQueue = new PriorityBlockingQueue<>(
            //생성자 매개변수1: initial capacity
            10,

            //생성자 매개변수2: sellOrderQueue의 comparator 객체
            new Comparator<Order>() {
                @Override
                public int compare(Order order1, Order order2) {
                    Double orderPrice1 = order1.getOrderPrice();
                    Double orderPrice2 = order2.getOrderPrice();

                    //판매 주문 우선순위 큐에서, 주문들은 그 주문가격이 더 쌀 수록 더 높은 우선순위를 가진다.
                    if (orderPrice1 < orderPrice2) {
                        return 1;
                    } else if (orderPrice1 > orderPrice2) {
                        return -1;
                    } else {
                        //주문 가격이 같은 경우, 먼저 제시된 주문이 우선순위를 가집니다.
                        if (order1.getOrderDatetime().isBefore(order2.getOrderDatetime())) {
                            return 1;
                        } else if (order1.getOrderDatetime().isAfter(order2.getOrderDatetime())) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                }
            });

    ///// 생성자 /////
    public SellOrderQueue(String coinCode) {
        this.coinCode = coinCode;
    }

    ///// 게터 /////
    public String getCoinCode() {
        return coinCode;
    }

    public Iterator<Order> getIterator() {
        return this.sellOrderQueue.iterator();
    }

    ///// 사용자 정의 메소드 /////
    /**
     * 매도 주문 큐에 주문을 삽입합니다.
     * @param order Order: 삽입할 주문
     * @throws RuntimeException
     * 다음과 같은 경우 예외가 발생합니다.<br>
     * (1) 만약 제시된 주문이 영속화되지 않은 상태인 경우<br>
     * (2) 만약 제시된 주문의 코인 종목과 큐의 코인 종목이 불일치하는 경우<br>
     * (3) 만약 매도 주문이 제시된 경우<br>
     * (4) 그 외에 내부적으로 우선순위 큐에 주문을 삽입하는 과정에서 예외가 발생하는 경우
     */
    public void push(Order order) throws RuntimeException {

        //주문 객체가 영속화 되었는 지 확인합니다.
        //영속화된 주문 객체만 주문 큐에 삽입할 수 있습니다.
        if (order.getId() == null) {
            throw new RuntimeException("영속화 되지 않은 주문은 체결자에게 제출할 수 없습니다.");
        }
        //제시된 주문의 코인 종목과 큐의 코인 종목이 불일치하는 경우
        if (!this.coinCode.equals(order.getCoin().getCode())) {
            throw new RuntimeException("매도 주문 큐[" + this.coinCode + "]에 제시된 주문[" + this.coinCode + "]을 삽입할 수 없습니다.");
        }
        //매도 주문이 제시된 경우
        if (order.getIsBuying()) {
            throw new RuntimeException("매도 주문 큐에 매수 주문을 삽입할 수 없습니다.");
        }

        //큐에 주문을 삽입
        this.sellOrderQueue.add(order);
    }

    /**
     * 매도 주문 큐에서 가장 우선순위가 높은 주문 하나를 조회합니다.
     * @return Order: 최고 우선순위 주문 객체, 만약 큐가 빈경우 null이 반환됩니다.
     */
    public Order peek() {
        return sellOrderQueue.peek();
    }

    /**
     * 매도 주문 큐에서 가장 우선순위가 높은 주문 하나를 큐에서 조회 및 삭제합니다.
     * @return Order: 큐에 저장되어 있던 최고 우선순위 주문 객체
     * @throws InterruptedException
     */
    public Order take() throws InterruptedException {
        return sellOrderQueue.take();
    }
}

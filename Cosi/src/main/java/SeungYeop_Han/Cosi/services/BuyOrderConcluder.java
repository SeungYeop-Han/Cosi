package SeungYeop_Han.Cosi.services;

import SeungYeop_Han.Cosi.agents.TradePriceStreamer;
import SeungYeop_Han.Cosi.domains.*;
import SeungYeop_Han.Cosi.repositories.MemberRepository;
import SeungYeop_Han.Cosi.repositories.OrderRepository;
import SeungYeop_Han.Cosi.repositories.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//매수 주문 체결 서비스 클래스
@Service
public class BuyOrderConcluder {

    ///// 의존성 주입 /////
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;

    @Autowired
    public BuyOrderConcluder(OrderRepository orderRepository,
                             MemberRepository memberRepository,
                             WalletRepository walletRepository) {
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
        this.walletRepository = walletRepository;
    }

    ///// 속성 /////
    // 각 종목 별 매수 체결 큐
    // ConcurrentHashMap<코인 코드, 매수 주문 큐>
    private final ConcurrentHashMap<String, BuyOrderQueue> buyOrderQueues
            = new ConcurrentHashMap<>();
    //실행 중인 스레드
    private final HashMap<String, String> runningThreads = new HashMap<>();

    ///// 사용자 정의 메소드 /////
    /**
     * 주문을 주문 큐에 삽입합니다.
     * @param order Order: 영속화된 주문
     * @throws RuntimeException
     */
    public void pushOrder(Order order) throws RuntimeException {

        //주문의 코인 종목을 식별
        String coinCode = order.getCoin().getCode();

        //체결 큐에서 해당 종목의 주문 큐를 가져옵니다.
        //만약 주어진 코인에 대한 주문 큐가 없다면, 새로 생성합니다.
        if ( ! this.buyOrderQueues.containsKey(coinCode)) {
            this.buyOrderQueues.put(coinCode, new BuyOrderQueue(coinCode));
        }
        BuyOrderQueue orderQueue = this.buyOrderQueues.get(coinCode);

        //매수 주문 큐에 주문을 삽입합니다.
        orderQueue.push(order);

        //기존에 존재하지 않는 경우에만, 해당 코인 종목에 대한 매수 체결 스레드를 생성합니다.
        if ( ! runningThreads.containsKey(coinCode)) {
            System.out.println("코인[" + coinCode + "]에 대한 매수 체결 스레드가 생성되었습니다.");
            runningThreads.put(coinCode, "");
            this.conclude(coinCode);
        }
    }


    /**
     * 제시된 코인 코드에 해당하는 종목에대한 체결 스레드를 생성 및 실행합니다.
     * @param coinCode String: 목표 코인 종목의 코드
     * @throws RuntimeException
     */
    public void conclude(String coinCode) throws RuntimeException {

        Timer scheduler = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //coinCode에 해당하는 종목의 현재가를 가져옵니다.
                Double tradePrice = TradePriceStreamer.getTradePrice(coinCode);

                /* 매수 주문 체결
                    해당 코인에 대한 매수 주문 큐에 존재하는 모든 주문들을 검사하여
                    주문 가격이 현재가 이상인 모든 주문들을 현재가에서 전량 체결한다. */

                // 1) 해당 코인에 대한 주문 큐가 존재하는 경우 해당 주문 큐와 반복자를 획득합니다.
                //      만약 존재치 않는 경우 예외를 던집니다.
                if (!buyOrderQueues.containsKey(coinCode)) {
                    throw new RuntimeException("코인 " + coinCode + "에 대한 매수 주문 큐가 존재하지 않습니다.");
                }
                BuyOrderQueue orderQueue = buyOrderQueues.get(coinCode);
                Iterator<Order> orderIterator = orderQueue.getIterator();

                // 2) 매수 주문 큐를 순회하면서 조건에 부합하는 주문들을 체결합니다.
                while (orderIterator.hasNext()) {
                    Order order = orderIterator.next();

                    // 2-1) 주문이 DB에 존재치 않는 경우, 주문 큐에서 삭제합니다. 예를 들어, 취소된 미체결 주문의 경우가 있을 수 있습니다.
                    if (orderRepository.findById(order.getId()).isEmpty()) {
                        orderIterator.remove();
                    }

                    // 2-2) 현재가보다 낮은 주문금액의 주문을 만난 경우 체결 스레드를 중지합니다.(이후 일정 시간 이후에 자동으로 다시 실행될 것 입니다.)
                    // 왜냐하면, 주문 큐는 우선순위 큐이며, 따라서 특정 주문의 조건이 부합하지 않는 경우 이후의 모든 주문에서도 동일할 것이기 때문입니다.
                    if (order.getOrderPrice() < tradePrice) {
                        break;
                    }

                    // 2-3) 조건에 부합하는 경우 체결합니다.
                    else{
                        // 체결 준비: 해당 주문의 주문자, 코인, 지갑을 가져옵니다.
                        Member orderer;
                        Optional<Member> memberOptional = orderRepository.findMemberByOrderId(order.getId());
                        if (memberOptional.isPresent()) {
                            orderer = memberOptional.get();
                        } else {
                            throw new RuntimeException("주문자가 존재하지 않습니다.");
                        }

                        Coin coin = order.getCoin();
                        if (coin == null) {
                            throw new RuntimeException("코인(" + coinCode + ")이 존재하지 않습니다.");
                        }

                        Wallet wallet;
                        Optional<Wallet> walletOptional = walletRepository.findWalletByMemberIdAndCoinId(orderer.getId(), order.getCoin().getId());
                        if (walletOptional.isPresent()) {
                            wallet = walletOptional.get();
                        } else {
                            throw new RuntimeException("지갑이 존재하지 않습니다.");
                        }

                        // 체결 실행
                        Double orderAmount = order.getOrderAmount();

                        // 코인 매수를 지갑에 반영합니다.
                        wallet.takeIn(tradePrice, orderAmount);

                        ////////////////////////////////////////// 체결 시 차액 반환 ///////////////////////////////////////////
                        // 주문 생성 시점에서의 주문총액 = 수량 * 주문 생성 시점에서의 주문금액
                        // -> 임시정산금액(매수 시 미리 묶여있던 돈) = 주문 생성 시 주문총액 + 주문 생성 시 주문총액에 대한 수수료
                        // 재산정된 주문총액 = 체결단가 * 체결수량
                        // -> 매수정산금액 = 재산정된 주문총액 + 재산정된 주문총액에 대한 수수료
                        // (재산정된 주문총액이 주문(Order) 엔티티의 주문총액(totalPrice) 필드를 덮어쓰지는 않습니다.)
                        //
                        // 이때, 실제 지불되어야 할 금액은 '매수정산금액' 이지만, 주문 생성 시 마켓에 선지불 된 금액은
                        // '임시정산금액' 이므로 차액을 반환합니다. (차액 = 임시정산금액 - 매수정산금액)
                        // 한편 주문 체결은 주문 생성 시 조건과 일치하거나 더 좋은 조건에서만 이루어지므로 차액이 음수가 되는 경우는 없습니다.
                        /////////////////////////////////////////////////////////////////////////////////////////////////////
                        Double feeRate = FeeRate.getInstance().getFeeRate(OrderType.LIMIT);
                        Double prepaymentCharge = order.getTotalPrice() * (1 + feeRate);

                        Double filledPrice = tradePrice * orderAmount;
                        Double paymentFee = filledPrice * feeRate;
                        Double paymentCharge = filledPrice + paymentFee;

                        orderer.earn(prepaymentCharge - paymentCharge);

                        // 체결 엔티티 생성 및 초기화
                        FilledOrder filledOrder = FilledOrder.builder()
                                .filledDatetime(LocalDateTime.now())
                                .filledAmount(orderAmount)
                                .filledPrice(filledPrice)
                                .unitPrice(tradePrice)
                                .fee(paymentFee)
                                .order(order)
                                .build();

                        // 주문 상태를 체결로 변경
                        order.setOrderState(OrderState.FILLED);
                        order.setFilledOrder(filledOrder);

                        // 데이터베이스에 반영(영속화)
                        memberRepository.save(orderer);
                        walletRepository.save(wallet);
                        orderRepository.save(order);

                        // 체결 완료 후 해당 주문을 큐에서 제거
                        orderIterator.remove();
                    }
                }
            }
        };

        //0.1초 뒤에 10초마다 한 번씩 체결 수행
        scheduler.scheduleAtFixedRate(task, 100, 1000);
    }
}

package SeungYeop_Han.Cosi.services;

import SeungYeop_Han.Cosi.DTOs.OrderSheet;
import SeungYeop_Han.Cosi.agents.TradePriceStreamer;
import SeungYeop_Han.Cosi.domains.*;
import SeungYeop_Han.Cosi.repositories.CoinRepository;
import SeungYeop_Han.Cosi.repositories.MemberRepository;
import SeungYeop_Han.Cosi.repositories.OrderRepository;
import SeungYeop_Han.Cosi.repositories.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    ///// 의존성 주입 /////
    private OrderRepository orderRepository;
    private CoinRepository coinRepository;
    private WalletRepository walletRepository;
    private MemberRepository memberRepository;

    private BuyOrderConcluder buyOrderConcluder;
    private SellOrderConcluder sellOrderConcluder;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        CoinRepository coinRepository,
                        WalletRepository walletRepository,
                        MemberRepository memberRepository,
                        BuyOrderConcluder buyOrderConcluder,
                        SellOrderConcluder sellOrderConcluder) {
        this.orderRepository = orderRepository;
        this.coinRepository = coinRepository;
        this.walletRepository = walletRepository;
        this.memberRepository = memberRepository;

        this.buyOrderConcluder = buyOrderConcluder;
        this.sellOrderConcluder = sellOrderConcluder;
    }


    ///// 사용자 정의 메소드 /////

    /**
     * 요청된 주문이 올바른 형식인지 검사합니다.
     * @param orderSheet
     * @return
     * @throws RuntimeException: 주문의 형식이 올바르지 않은 경우 예외를 던집니다.
     */
    public boolean checkOrderSheet(OrderSheet orderSheet) throws RuntimeException {

        //주문수량 최소단위(1 사토시)
        final Double ONE_SATOSHI = 0.00000001;

        //orderSheet 필드들을 전부 지역변수로 복사
        Boolean isBuying = orderSheet.getIsBuying();
        OrderType orderType = orderSheet.getOrderType();
        String coinCode = orderSheet.getCoinCode();
        Double stopPrice = orderSheet.getStopPrice();
        Double orderPrice = orderSheet.getOrderPrice();
        Double orderAmount = orderSheet.getOrderAmount();
        Double totalPrice = orderSheet.getTotalPrice();

        //모든 주문 공통
        if (isBuying == null) {
            throw new RuntimeException("잘못된 주문 형식: 매수/매도 여부가 명시되지 않았습니다.");
        }
        if ( ! List.of("MARKET", "LIMIT", "STOP_LIMIT").contains(orderType.name())) {
            throw new RuntimeException("잘못된 주문 형식: 주문 유형이 올바르게 명시되지 않았습니다. 주문형식은 \"MARKET\", \"LIMIT\", \"STOP-LIMIT\" 중 하나여야 합니다.");
        }
        if (coinRepository.findCoinByCode(coinCode).isEmpty()) {
            throw new RuntimeException("잘못된 주문 형식: 코인이 명시되어 있지 않거나, 명시된 코인이 지원되지 않는 상태입니다.");
        }

        //시장가
        if (orderType == OrderType.MARKET) {

            //시장가 공통
            if (stopPrice != null) {
                throw new RuntimeException("잘못된 시장가 주문 형식: 감시가격이 명시되어서는 안됩니다. 감시가격 필드는 null 이어야 합니다.");
            }
            if (orderPrice != null) {
                throw new RuntimeException("잘못된 시장가 주문 형식: 주문가격이 명시되어서는 안됩니다. 주문가격 필드는 null 이어야 합니다.");
            }

            //시장가 매수
            if (isBuying) {
                if (orderAmount != null) {
                    throw new RuntimeException("잘못된 시장가 매수 주문 형식: 주문수량이 명시되어서는 안됩니다. 주문수량 필드는 null 이어야 합니다.");
                }
                if (totalPrice <= 0) {
                    throw new RuntimeException("잘못된 시장가 매수 주문 형식: 주문 총액은 양수여야 합니다.");
                }
            }
            //시장가 매도
            else {
                if (totalPrice != null) {
                    throw new RuntimeException("잘못된 시장가 매도 주문 형식: 주문총액이 명시되어서는 안됩니다. 주문총액 필드는 null 이어야 합니다.");
                }
                if (orderAmount < ONE_SATOSHI) {
                    throw new RuntimeException("잘못된 시장가 매도 주문 형식: 주문수량은 최소 1 satoshi(0.00000001) 이상이어야 합니다.");
                }
            }
        }

        //지정가(지정가 주문은 매수/매도에 따른 형식 검사의 차이가 없습니다.)
        else if (orderType == OrderType.LIMIT) {
            if (stopPrice != null) {
                throw new RuntimeException("잘못된 지정가 주문 형식: 감시가격은 명시되어서는 안 됩니다. 감시가격 필드는 null 이어야 합니다.");
            }
            if (orderPrice < 0) {
                throw new RuntimeException("잘못된 지정가 주문 형식: 주문가격은 0보다 커야합니다.");
            }
            if (orderAmount < ONE_SATOSHI) {
                throw new RuntimeException("잘못된 지정가 주문 형식: 주문수량은 1 satoshi 이상이어야 합니다.");
            }

            //orderSheet에 명시된 주문가격, 주문수량, 그리고 주문총액 사이에 일관성이 있는지 검사합니다.
            //명시된 주문총액 == 주문가격 * 주문수량 이어야합니다.
            Double derivedTotalPrice = orderPrice * orderAmount;
            if ( ! derivedTotalPrice.equals(totalPrice)) {
                throw new RuntimeException("잘못된 지정가 주문 형식: 주문서에 명시된 주문가격, 주문수량, 그리고 주문총액 사이에 일관성이 없습니다. \n 주문가격 x 주문수량 = " + derivedTotalPrice + " 이지만, 주문총액= " + totalPrice + " 입니다.");
            }
        }

        //예약 지정가
        else if (orderType == OrderType.STOP_LIMIT) {
            if (stopPrice == null) {
                throw new RuntimeException("잘못된 예약 지정가 주문 형식: 감시가격이 명시되어 있지 않습니다.");
            }
            if (orderPrice > 0) {
                throw new RuntimeException("잘못된 예약 지정가 주문 형식: 주문가격은 0보다 커야합니다.");
            }
            if (orderAmount < ONE_SATOSHI) {
                throw new RuntimeException("잘못된 예약 지정가 주문 형식: 주문수량은 1 satoshi 이상이어야 합니다.");
            }

            //orderSheet에 명시된 주문가격, 주문수량, 그리고 주문총액 사이에 일관성이 있는지 검사합니다.
            //명시된 주문총액 == 주문가격 * 주문수량 이어야합니다.
            Double derivedTotalPrice = orderPrice * orderAmount;
            if ( ! derivedTotalPrice.equals(totalPrice)) {
                throw new RuntimeException("잘못된 예약 지정가 주문 형식: 주문서에 명시된 주문가격, 주문수량, 그리고 주문총액 사이에 일관성이 없습니다. \n 주문가격 x 주문수량 = " + derivedTotalPrice + " 이지만, 주문총액= " + totalPrice + " 입니다.");
            }
        }

        return true;
    }

    /**
     * 주문을 생성합니다. 본 메소드는 자신의 checkOrderSheet 메소드를 이용하여 주문 형식 검증을 포함합니다.
     * @param orderer
     * @param orderSheet
     * @throws RuntimeException
     */
    @Transactional
    public void order(Member orderer, OrderSheet orderSheet) throws RuntimeException {

        //orderSheet 필드들을 전부 지역변수로 복사
        Boolean isBuying = orderSheet.getIsBuying();
        OrderType orderType = orderSheet.getOrderType();
        String coinCode = orderSheet.getCoinCode();
        Double stopPrice = orderSheet.getStopPrice();
        Double orderPrice = orderSheet.getOrderPrice();
        Double orderAmount = orderSheet.getOrderAmount();
        Double totalPrice = orderSheet.getTotalPrice();

        // 1) 주문서(orderSheet) 형식 검사
        checkOrderSheet(orderSheet);

        // 2) 주문 대상 코인 가져오기
        Coin coin;
        Optional<Coin> coinOptional = coinRepository.findCoinByCode(coinCode);
        if (coinOptional.isPresent()) {
            coin = coinOptional.get();
        } else {
            throw new RuntimeException("주문에 실패하였습니다. (사유: 주문 대상이 되는 코인을 가져올 수 없습니다");
        }

        // 3) 주문자 지갑 엔티티 가져오기
        Wallet wallet;
        Optional<Wallet> walletOptional= walletRepository.findWalletByMemberIdAndCoinId(
                orderer.getId(), coin.getId()
        );
        if (walletOptional.isPresent()) {
            wallet = walletOptional.get();
        } else {
            //주문자가 해당 코인에 대한 지갑을 보유하고 있지 않다면 '빈' 지갑을 새로 생성한다.
            wallet = new Wallet(orderer, coin, 0.0, 0.0);
            walletRepository.save(wallet);
        }

        // 4) 주문 엔티티 생성
        Double feeRate = FeeRate.getInstance().getFeeRate( orderType );
        Order order = Order.builder()
                .isBuying( isBuying )
                .orderType( orderType )
                .orderState( OrderState.UNFILLED )
                .orderDatetime( LocalDateTime.now() )
                .stopPrice( stopPrice )
                .orderAmount( orderAmount )
                .orderPrice( orderPrice )
                .totalPrice( totalPrice )
                .member( orderer )
                .coin( coin )
                .build();

        // 5) 주문 유형 별 처리
        if (orderType == OrderType.MARKET) {
            if (isBuying) {
                fulfillMarketBuyOrder(orderer, wallet, order);
            } else {
                fulfillMarketSellOrder(orderer, wallet, order);
            }

        } else if (orderType == OrderType.LIMIT) {
            if (isBuying) {
                registerLimitBuyOrder(orderer, wallet, order);
            } else {
                registerLimitSellOrder(orderer, wallet, order);
            }

        } else {
            if (isBuying) {
                registerStopLimitBuyOrder(orderer, wallet, order);
            } else {
                registerStopLimitSellOrder(orderer, wallet, order);
            }
        }
    }

    /**
     * 주문을 취소합니다. 
     * @param requester : Member, 주문 취소 요청자 회원 엔티티입니다.
     * @param order: Order, 취소할 주문 엔티티입니다.
     * @throws RuntimeException
     */
    @Transactional
    public void cancelOrder(Member requester, Order order) throws RuntimeException {
        // 1) 주문자를 불러옵니다.
        Member orderer;
        Optional<Member> memberOptional = memberRepository.findById(order.getMember().getId());
        if (memberOptional.isPresent()) {
            orderer = memberOptional.get();
        } else {
            throw new RuntimeException("주문 취소 실패: 주문자를 불러올 수 없습니다.");
        }

        // 2) 주문자와 주문취소요청자의 일치 여부를 확인합니다.
        // 2-1) 일치하지 않는 경우
        if (!requester.getId().equals(orderer.getId())) {
            throw new RuntimeException("주문 취소 실패: 주문자와 요청자가 일치하지 않습니다");
        }

        // 2-2) 일치하는 경우
        else {
            // a) 매수주문취소
            if (order.getIsBuying()) {
                Double feeRate = FeeRate.getInstance().getFeeRate(OrderType.LIMIT);
                orderer.earn(order.getTotalPrice() * (1 + feeRate));
            }

            // b) 매도주문취소
            else {
                //지갑 불러오기
                Wallet wallet;
                Optional<Wallet> walletOptional = walletRepository.findWalletByMemberIdAndCoinId(
                        orderer.getId(), order.getCoin().getId()
                );

                //지갑이 존재하는 경우
                if (walletOptional.isPresent()) {
                    wallet = walletOptional.get();
                    wallet.cancelTakeOut(order.getOrderAmount());
                }

                //지갑이 존재하지 않는 경우
                else {
                    throw new RuntimeException("주문 취소 실패: 지갑을 불러올 수 없습니다.");
                }
            }

            //DB에서 주문을 삭제한다.
            //매수 또는 매도 체결자(concluder)에 있는 해당 코인에 대한 주문 큐에 해당 주문이 이미 들어가 있을 수 있지만,
            //지정가 체결 전에 항상 DB에 존재하는 지의 여부를 검사하기 때문에 해당 주문이 체결되는 일은 없다.
            orderRepository.delete(order);
        }

    }

    /**
     * 시장가 매수 주문을 체결합니다. 지정가 주문이나, 예약-지정가 주문과는 다르게 시장가 주문은 즉시 체결처리 합니다.
     * @param orderer
     * @param wallet
     * @param order
     * @throws RuntimeException
     */
    @Transactional
    public void fulfillMarketBuyOrder(Member orderer, Wallet wallet, Order order) throws RuntimeException {

        // 1) 실시간 현재가 가져오기
        String code = order.getCoin().getCode();
        Double tradePrice = TradePriceStreamer.getTradePrice(code);
        if (tradePrice == null) {
            throw new RuntimeException("시장가 매수 주문 실패: 코인 " + code + "에 대한 현재가를 불러올 수 없습니다.");
        }

        // 2) 시장가 주문 수수료율 가져오기
        Double feeRate = FeeRate.getInstance().getFeeRate(OrderType.MARKET);

        // 3) 주문자의 현재 잔고 및 매수 정산 금액(주문총액 + 수수료) 계산
        Double totalPrice = order.getTotalPrice();
        Double fee = totalPrice * feeRate;
        Double seed = orderer.getSeed();
        Double charge = totalPrice  + fee;

        // 4) 주문 가능 여부 확인
        if (totalPrice < 5000.0) {
            throw new RuntimeException("시장가 매수 주문 실패: 최소 주문금액은 5000원 이상이어야 합니다.");
        }
        if (seed < charge) {
            throw new RuntimeException("시장가 매수 주문 실패: 보유 시드가 부족합니다. 보유 시드의 양은 " + seed + "원 이지만 매수 정산 금액은 " + charge + "원 입니다.");
        }

        // 5) 매수 주문을 처리합니다.
        //  5-1) 주문총액과 주문가격(현재가)로부터 주문수량 계산(1 satoshi 단위에서 반 올림)
        Double orderAmount = Math.round(totalPrice / tradePrice * 100000000.0) / 100000000.0;

        //  5-2) 주문 내용 반영
        order.setOrderAmount(orderAmount);
        order.setOrderPrice(tradePrice);

        //  5-3) 시드 차감
        orderer.spend(charge);

        //  5-4) 지갑에 코인 추가
        wallet.takeIn(tradePrice, orderAmount);

        //  5-5) 체결 엔티티 생성 및 초기화
        FilledOrder filledOrder = FilledOrder.builder()
                .filledDatetime(LocalDateTime.now())
                .filledAmount(orderAmount)
                .filledPrice(totalPrice)
                .unitPrice(tradePrice)
                .fee(fee)
                .order(order)
                .build();

        //  5-6) 주문상태를 체결로 변경
        order.setOrderState(OrderState.FILLED);
        order.setFilledOrder(filledOrder);

        //  5-7) 데이터베이스에 영속화
        memberRepository.save(orderer);
        walletRepository.save(wallet);
        orderRepository.save(order);
    }

    /**
     * 시장가 매도 주문을 체결합니다. 지정가 주문이나, 예약-지정가 주문과는 다르게 시장가 주문은 즉시 체결처리 합니다.
     * @param orderer
     * @param wallet
     * @param order
     * @throws RuntimeException
     */
    @Transactional
    public void fulfillMarketSellOrder(Member orderer, Wallet wallet, Order order) throws RuntimeException {

        // 1) 실시간 현재가 가져오기
        String code = order.getCoin().getCode();
        Double tradePrice = TradePriceStreamer.getTradePrice(code);
        if (tradePrice == null) {
            throw new RuntimeException("시장가 매도 주문 실패: 코인 " + code + "에 대한 현재가를 불러올 수 없습니다.");
        }

        // 2) 시장가 주문 수수료율 가져오기
        Double feeRate = FeeRate.getInstance().getFeeRate(OrderType.MARKET);

        // 3) 주문수량과 현재가를 이용하여 주문총액과 수수료를 계산
        Double currentAmount = wallet.getAmount();
        Double orderAmount = order.getOrderAmount();
        Double totalPrice = orderAmount * tradePrice;
        Double fee = totalPrice * feeRate;

        // 4) 주문 가능 여부 확인
        if (totalPrice < 5000.0) {
            throw new RuntimeException("시장가 매도 주문 실패: 최소 주문금액은 5000원 이상이어야 합니다.");
        }
        if (currentAmount < orderAmount) {
            throw new RuntimeException("시장가 매도 주문 실패: 보유 수량이 부족합니다. 보유 " + code + "의 양은 " + currentAmount + "개 이지만 매도 수량은 " + orderAmount + "개 입니다.");
        }

        // 5) 매도 주문을 처리합니다.
        //  5-1) 주문 내용 반영
        order.setOrderPrice(tradePrice);
        order.setTotalPrice(totalPrice);

        //  5-2) 지갑에서 코인 수량 차감
        wallet.takeOut(orderAmount);

        //  5-3) 시드 추가
        Double earnings = totalPrice - fee;
        orderer.earn(earnings);

        //  5-3) 체결 엔티티 생성 및 초기화
        FilledOrder filledOrder = FilledOrder.builder()
                .filledDatetime(LocalDateTime.now())
                .filledAmount(orderAmount)
                .filledPrice(totalPrice)
                .unitPrice(tradePrice)
                .fee(fee)
                .order(order)
                .build();

        //  5-6) 주문상태를 체결로 변경
        order.setOrderState(OrderState.FILLED);
        order.setFilledOrder(filledOrder);

        //  5-7) 데이터베이스에 영속화
        memberRepository.save(orderer);
        walletRepository.save(wallet);
        orderRepository.save(order);
    }

    /**
     * 지정가 매수 주문을 체결자에 등록합니다.
     * @param orderer
     * @param wallet
     * @param order
     * @throws RuntimeException
     */
    @Transactional
    public void registerLimitBuyOrder(Member orderer, Wallet wallet, Order order) throws RuntimeException {

        // 1) 수수료와 마켓에 묶일 돈을 계산
        // 이 과정에서 수수료를 계산하기는 하지만, 해당 수수료는 임시정산금액에 대한 수수료이므로 확정 수수료가 아니므로 order 엔티티에 반영되지 않습니다.
        Double seed = orderer.getSeed();
        Double totalPrice = order.getTotalPrice();
        Double feeRate = FeeRate.getInstance().getFeeRate(OrderType.LIMIT);
        Double fee = totalPrice * feeRate;
        Double charge = totalPrice + fee;

        // 2) 주문 가능 여부 확인
        if (totalPrice < 5000.0) {
            throw new RuntimeException("지정가 매수 주문 실패: 최소 주문금액은 5000원 이상이어야 합니다.");
        }
        if (seed < charge) {
            throw new RuntimeException("지정가 매수 주문 실패: 보유 시드가 부족합니다. 보유 시드의 양은 " + seed + "원 이지만 매수 정산 금액은 " + charge + "원 입니다.");
        }

        // 3) 마켓에 시드 지불
        //지정가 주문의 경우 항상 등록된 조건보다 좋은 조건으로 체결되므로, 추후 그에 따른 차액은 반환됩니다.
        orderer.spend(charge);

        // 4) 데이터베이스에 반영
        memberRepository.save(orderer);
        orderRepository.save(order);

        // 5) 주문 처리를 체결자에게 위임
        try {
            buyOrderConcluder.pushOrder(order);

        } catch (Exception e) {
            orderer.earn(charge);
            memberRepository.save(orderer);
            orderRepository.deleteById(order.getId());

            throw e;
        }
    }

    /**
     * 지정가 매도 주문을 체결자에 등록합니다.
     *
     * @param orderer
     * @param wallet
     * @param order
     * @throws RuntimeException
     */
    @Transactional
    public void registerLimitSellOrder(Member orderer, Wallet wallet, Order order) throws RuntimeException {

        // 1) 보유수량과 주문수량 획득
        Double currentAmount = wallet.getAmount();
        Double orderAmount = order.getOrderAmount();

        // 2) 주문 가능 여부 확인
        if (order.getOrderPrice() < 5000.0) {
            throw new RuntimeException("지정가 매도 주문 실패: 최소 주문금액은 5000원 이상이어야 합니다.");
        }
        if (currentAmount < orderAmount) {
            throw new RuntimeException("지정가 매도 주문 실패: 보유 수량이 부족합니다. 보유 수량은 " + currentAmount + "개 이지만, 주문 수량은 " + orderAmount + "개 입니다.");
        }

        // 3) 주문수량만큼 지갑에서 차감
        wallet.takeOut(orderAmount);

        // 4) 데이터베이스에 반영
        memberRepository.save(orderer);
        walletRepository.save(wallet);
        orderRepository.save(order);

        // 5) 주문 처리를 체결자에게 위임
        try {
            sellOrderConcluder.pushOrder(order);

        } catch (Exception e) {
            wallet.cancelTakeOut(orderAmount);
            memberRepository.save(orderer);
            walletRepository.save(wallet);
            orderRepository.deleteById(order.getId());

            throw e;
        }
    }

    /**
     *
     * @param orderer
     * @param wallet
     * @param order
     * @throws RuntimeException
     */
    @Transactional
    public void registerStopLimitBuyOrder(Member orderer, Wallet wallet, Order order) throws RuntimeException {

    }

    /**
     *
     * @param orderer
     * @param wallet
     * @param order
     * @throws RuntimeException
     */
    @Transactional
    protected void registerStopLimitSellOrder(Member orderer, Wallet wallet, Order order) throws RuntimeException {

    }
}

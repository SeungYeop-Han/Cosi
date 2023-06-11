package SeungYeop_Han.Cosi.controllers;

import SeungYeop_Han.Cosi.DTOs.OrderSheet;
import SeungYeop_Han.Cosi.domains.Member;
import SeungYeop_Han.Cosi.domains.Order;
import SeungYeop_Han.Cosi.domains.OrderType;
import SeungYeop_Han.Cosi.repositories.CoinRepository;
import SeungYeop_Han.Cosi.repositories.MemberRepository;
import SeungYeop_Han.Cosi.repositories.OrderRepository;
import SeungYeop_Han.Cosi.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class MarketController {

    private MemberRepository memberRepository;
    private OrderRepository orderRepository;
    private CoinRepository coinRepository;
    private OrderService orderService;

    @Autowired
    public MarketController(MemberRepository memberRepository,
                            OrderRepository orderRepository,
                            CoinRepository coinRepository,
                            OrderService orderService) {
        this.memberRepository = memberRepository;
        this.orderRepository = orderRepository;
        this.coinRepository = coinRepository;
        this.orderService = orderService;
    }

    /**
     * 거래소 입장 요청에 응답합니다.
     *
     * @param member
     * @param model
     * @return
     */
    @GetMapping("/market")
    public String enterMarket(@AuthenticationPrincipal Member member, Model model) {

        Member targetMember;
        Optional<Member> memberOptional = memberRepository.findById(member.getId());
        if (memberOptional.isPresent()) {
            targetMember = memberOptional.get();
        } else {
            model.addAttribute("errMsg", "회원을 찾을 수 없습니다.");
            return "/error";
        }

        String name = targetMember.getName();
        String email = targetMember.getEmail();
        int seed = member.getSeed().intValue();

        model.addAttribute("userName", name);
        model.addAttribute("userEmail", email);
        model.addAttribute("seed", seed);

        return "/market";
    }

    /**
     * 주문 요청에 응답합니다.
     * @param member
     * @param orderSheet
     * @param model
     * @return
     */
    @ResponseBody
    @PostMapping("/order")
    public String order(@AuthenticationPrincipal Member member, @RequestBody OrderSheet orderSheet, Model model) {

        //반환 메시지
        String returnMsg;

        try {

            //예약 지정가 주문은 아직 지원하지 않습니다.
            if (orderSheet.getOrderType().equals(OrderType.STOP_LIMIT)) {
                returnMsg = "예약-지정가 주문은 곧 서비스될 예정입니다.";
            }

            //회원 가져오기
            Member targetMember;
            Optional<Member> memberOptional = memberRepository.findById(member.getId());

            //회원을 찾은 경우에만 주문을 요청한다.
            if (memberOptional.isPresent()) {
                targetMember = memberOptional.get();
                orderService.order(targetMember, orderSheet);
                returnMsg = "주문에 성공하였습니다!";

            } else {
                returnMsg = "회원을 찾을 수 없습니다.";
            }

        } catch (Exception e) {
            returnMsg = e.getMessage();
        }

        return returnMsg;
    }

    /**
     * 주문 취소에 응답한다.
     * @param member
     * @param orderId
     * @return
     */
    @ResponseBody
    @DeleteMapping("/order")
    public String cancelOrder(@AuthenticationPrincipal Member member, @RequestParam("orderId") Long orderId) {

        String returnMsg;

        try {
            //회원 불러오기
            Member targetMember;
            Optional<Member> memberOptional = memberRepository.findById(member.getId());
            if (memberOptional.isPresent()) {
                targetMember = memberOptional.get();
            } else {
                return "주문 취소 실패: 주문자를 불러올 수 없습니다.";
            }

            //주문 불러오기
            Order targetOrder;
            Optional<Order> orderOptional = orderRepository.findById(orderId);
            if (orderOptional.isPresent()) {
                targetOrder = orderOptional.get();
                orderService.cancelOrder(targetMember, targetOrder);
            } else {
                return "주문 취소 실패: 주문을 불러올 수 없습니다.";
            }
        } catch (Exception e) {
            return e.getMessage();
        }

        return "주문이 정상적으로 취소되었습니다.";
    }
}

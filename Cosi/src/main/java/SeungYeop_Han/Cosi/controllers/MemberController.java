package SeungYeop_Han.Cosi.controllers;

import SeungYeop_Han.Cosi.domains.Member;
import SeungYeop_Han.Cosi.domains.Wallet;
import SeungYeop_Han.Cosi.repositories.*;
import SeungYeop_Han.Cosi.services.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

@Controller
public class MemberController {
    private CoinRepository coinRepository;
    private MemberRepository memberRepository;
    private WalletRepository walletRepository;
    private OrderRepository orderRepository;
    private FilledOrderRepository filledOrderRepository;
    private ConfirmationTokenRepository confirmationTokenRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private MemberService memberService;

    @Autowired
    public MemberController(CoinRepository coinRepository,
                            MemberRepository memberRepository,
                            WalletRepository walletRepository,
                            OrderRepository orderRepository,
                            FilledOrderRepository filledOrderRepository,
                            ConfirmationTokenRepository confirmationTokenRepository,
                            BCryptPasswordEncoder bCryptPasswordEncoder,
                            MemberService memberService) {
        this.coinRepository = coinRepository;
        this.memberRepository = memberRepository;
        this.walletRepository = walletRepository;
        this.orderRepository = orderRepository;
        this.filledOrderRepository = filledOrderRepository;
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.memberService = memberService;
    }


    /**
     * 내 정보 조회 요청에 응답합니다.
     *
     * @param member
     * @param model
     * @return 내 정보 페이지를 반환합니다. 만약 실패하면 에러페이지를 반환합니다.
     */
    @GetMapping("/member")
    public String showMemberInfo(@AuthenticationPrincipal Member member, Model model) {
        try {
            // 1) 회원 가져오기
            Member targetMember;
            Optional<Member> memberOptional = memberRepository.findById(member.getId());

            // 1-1) 회원이 존재하는 경우
            if (memberOptional.isPresent()) {
                targetMember = memberOptional.get();

                model.addAttribute("email", targetMember.getEmail());
                model.addAttribute("name", targetMember.getEmail());
                model.addAttribute("seed", targetMember.getSeed());

                return "/user-info";
            }

            // 1-2) 회원이 존재하지 않는 경우
            else {
                model.addAttribute("errMsg", "회원정보 조회 실패: 회원을 불러올 수 없습니다.");
                return "/error";
            }

        } catch (Exception e) {
            model.addAttribute("errMsg", "회원정보 조회 실패: " + e.getMessage());
            return "/error";
        }
    }

    /**
     * 패스워드 변경 요청에 응답합니다.
     *
     * @param password
     * @param newPassword
     * @param member
     * @return: 실패 혹은 성공 메시지 문자열을 반환합니다.
     */
    @ResponseBody
    @PutMapping("/member/password")
    public String changePassword(@RequestParam("password") String password,
                                 @RequestParam("newPassword") String newPassword,
                                 @AuthenticationPrincipal Member member) {
        try {
            // 1) 회원 불러오기
            Member targetMember;
            Optional<Member> memberOptional = memberRepository.findById(member.getId());
            if (memberOptional.isPresent()) {
                targetMember = memberOptional.get();
            } else {
                return "비밀번호 변경 실패: 회원을 불러올 수 없습니다.";
            }

            // 2) 패스워드 일치여부 확인
            String encodedPassword = targetMember.getPassword();

            // 2-1) 패스워드가 일치하지 않는 경우
            if (!bCryptPasswordEncoder.matches(password, encodedPassword)) {
                return "사용자 비밀번호가 일치하지 않습니다.";
            }

            // 2-2) 패스워드가 일치하는 경우
            else {
                // 제시된 새로운 패스워드를 암호화합니다.
                String encodedNewPassword = bCryptPasswordEncoder.encode(newPassword);

                // a) 새 패스워드가 기존 패스워드와 일치하는 경우
                if (bCryptPasswordEncoder.matches(newPassword, encodedPassword)) {
                    return "기존 비밀번호와 동일합니다. 다른 비밀번호를 입력해주세요.";
                }

                // b) 새 패스워드의 형식이 유효하지 않은 경우
                if (!memberService.testPassword(newPassword)) {
                    return "비밀번호는 8 ~ 12자 길이, 최소 하나의 문자 및 하나의 숫자로 이루어져야 합니다.";
                }

                // c) 새 패스워드가 기존 패스워드와 일치하지도 않고, 그 형식이 유효한 경우
                // 기존 패스워드를 새 패스워드로 변경합니다.
                targetMember.setPassword(encodedNewPassword);
                memberRepository.save(targetMember);

                return "패스워드 변경이 완료되었습니다.";
            }
        } catch (Exception e) {
            return "패스워드 변경 실패: " + e.getMessage();
        }
    }

    /**
     * 자산 정보 조회 요청에 응답합니다.
     *
     * @param member
     * @param model
     * @return 자산 정보 페이지를 반환하거나, 에러 페이지를 반환합니다.
     */
    @GetMapping("/member/asset")
    public String showAssetList(@AuthenticationPrincipal Member member, Model model) {

        try {
            // 1) 회원 불러오기
            Member targetMember;
            Optional<Member> memberOptional = memberRepository.findById(member.getId());
            if (memberOptional.isPresent()) {
                targetMember = memberOptional.get();
            } else {
                model.addAttribute("errMsg", "자산정보 조회 실패: 회원을 불러올 수 없습니다.");
                return "/error";
            }

            // 2) 지갑 목록 불러오기
            List<Wallet> wallets = walletRepository.findWalletsByMemberId(targetMember.getId());

            // 3) 시드 가져오기
            int seed = targetMember.getSeed().intValue();

            model.addAttribute("wallets", wallets);
            model.addAttribute("seed", seed);
            return "/asset";

        } catch (Exception e) {
            model.addAttribute("errMsg", e.getMessage());
            return "/error";
        }
    }

    /**
     * 미체결 주문 목록 요청에 응답합니다.
     *
     * @param member
     * @return
     */
    @ResponseBody
    @GetMapping("/member/unfilledOrders")
    public ResponseEntity<List<?>> showUnfilledOrders(@AuthenticationPrincipal Member member) {
        try {
            //미체결 목록 반환
            List<?> unfilledOrders = orderRepository.findUnfilledOrdersByMemberId(member.getId());
            return ResponseEntity.ok().body(unfilledOrders);

        } catch (Exception e) {
            System.out.println("At showUnfilledOrders() " + e.getMessage() + " *****");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 체결 주문 목록 요청에 응답합니다.
     *
     * @param member
     * @return
     */
    @ResponseBody
    @GetMapping("/member/filledOrders")
    public ResponseEntity<List<?>> showFilledOrders(@AuthenticationPrincipal Member member) {
        try {
            //미체결 목록 반환
            List<?> filledOrders = orderRepository.findFilledOrdersByMemberId(member.getId());
            return ResponseEntity.ok().body(filledOrders);

        } catch (Exception e) {
            System.out.println("At showFilledOrders() " + e.getMessage() + " *****");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 계정 정보를 제외한 모든 투자 정보를 초기화합니다.
     *
     * @param member
     * @return 성공 혹은 실패 메시지를 반환합니다.
     */
    @ResponseBody
    @PutMapping("/reset")
    public String reset(@AuthenticationPrincipal Member member) {
        try {
            // 1) 회원 불러오기
            Member targetMember;
            Optional<Member> memberOptional = memberRepository.findById(member.getId());
            if (memberOptional.isPresent()) {
                targetMember = memberOptional.get();
            } else {
                return "계정 초기화 실패: 회원을 불러올 수 없습니다.";
            }

            // 2) 자산 정보 초기화
            memberService.initializeSeed(targetMember);

            // 3) 해당 회원의 모든 체결 내역을 삭제합니다.
            orderRepository.findOrdersByMemberId(targetMember.getId())
                    .forEach(o -> {
                        filledOrderRepository.deleteFilledOrdersByOrderId(o.getId());
                    });

            // 4) 해당 회원의 모든 주문 내역을 삭제합니다.
            orderRepository.deleteOrdersByMemberId(member.getId());

            // 5) 회원이 보유 중인 모든 지갑을 삭제합니다.
            walletRepository.deleteWalletsByMemberId(targetMember.getId());

            return "계정이 성공적으로 초기화되었습니다.";

        } catch (Exception e) {
            return "계정 초기화 실패: " + e.getMessage();
        }
    }

    /**
     * 계정 탈퇴 요청에 응답합니다.
     * @param member
     * @return 계정 탈퇴 페이지를 반환합니다.
     */
    // ToDo: @DeleteMapping("/member")로 처리하려 했으나 잘 작동하지 않는 부분이 있어서 추후 해결해야 함
    @GetMapping("/hell")
    public String withdraw(@AuthenticationPrincipal Member member) {
        try {
            // 1) 자산정보 초기화
            reset(member);

            // 2) 인증 토큰 삭제
            confirmationTokenRepository.deleteConfirmationTokensByMemberId(member.getId());

            // 3) 회원 삭제
            memberRepository.deleteById(member.getId());

            // 4) 계정 탈퇴 페이지 반환
            return "/goodbye";

        } catch (Exception e) {
            return "계정 탈퇴 실패: " + e.getMessage();
        }
    }
}

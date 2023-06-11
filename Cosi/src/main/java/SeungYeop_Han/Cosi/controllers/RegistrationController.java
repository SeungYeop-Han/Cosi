package SeungYeop_Han.Cosi.controllers;

import SeungYeop_Han.Cosi.DTOs.RegistrationRequest;
import SeungYeop_Han.Cosi.domains.Member;
import SeungYeop_Han.Cosi.repositories.ConfirmationTokenRepository;
import SeungYeop_Han.Cosi.services.ConfirmationTokenService;
import SeungYeop_Han.Cosi.services.MemberService;
import SeungYeop_Han.Cosi.services.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping(path = "registration")
public class RegistrationController {

    private RegistrationService registrationService;
    private MemberService memberService;
    private ConfirmationTokenRepository confirmationTokenRepository;
    private ConfirmationTokenService confirmationTokenService;

    @Autowired
    public RegistrationController(RegistrationService registrationService,
                                  MemberService memberService,
                                  ConfirmationTokenRepository confirmationTokenRepository,
                                  ConfirmationTokenService confirmationTokenService) {
        this.registrationService = registrationService;
        this.memberService = memberService;
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.confirmationTokenService = confirmationTokenService;
    }

    /**
     * 회원가입 폼 요청에 응답합니다.
     *
     * @return
     */
    @GetMapping
    public String signUp() {
        return "/registration";
    }

    /**
     * 이메일 중복 확인 요청에 응답합니다.
     *
     * @param email
     * @return
     */
    @GetMapping("/isEmailDuplicated")
    @ResponseBody
    public String signUp(@RequestParam("email") String email) {
        if (email.isEmpty()) {
            return "이메일을 입력해주세요.";
        } else {
            return memberService.evaluateEmail(email);
        }
    }

    /**
     * 회원가입 요청에 대해 응답합니다.
     *
     * @param request
     * @param model
     * @return 회원가입 확정을 위한 이메일 인증 페이지를 반환하거나, 에러 페이지를 반환합니다.
     */
    @PostMapping
    public String register(@ModelAttribute RegistrationRequest request, Model model) {
        try {
            System.out.println("start register: " + request);
            registrationService.register(request);
            model.addAttribute("msg", "등록한 이메일로 인증 요청 메일이 전송되었습니다.<br>15분 내에 인증 링크를 클릭해주십시오.");
        } catch (Exception e) {
            model.addAttribute("msg", "계정 등록 실패: " + e.getMessage());
        }

        return "/request-email-confirmation";
    }

    /**
     * 이메일 인증 완료 요청에 응답합니다.
     * @param token
     * @param model
     * @return
     */
    @GetMapping(path = "confirmed")
    public String confirmed(@RequestParam("token") String token, Model model) {
        try {
            // 1) 회원 불러오기
            Member targetMember;
            Optional<Member> memberOptional = confirmationTokenRepository.findMemberByToken(token);
            if (memberOptional.isPresent()) {
                targetMember = memberOptional.get();
            } else {
                model.addAttribute("errMsg", "이메일 인증 실패: 회원을 찾을 수 없습니다.");
                return "/error";
            }

            // 2) 인증 토큰 승인
            confirmationTokenService.confirm(token);

            // 3) 이메일 인증 완료 페이지 반환
            model.addAttribute("name", targetMember.getName());
            return "/complete-email-confirmation";

        } catch (Exception e) {
            model.addAttribute("errMsg", "이메일 인증 실패: 유효하지 않거나 만료된 인증입니다. <br>사유: " + e.getMessage());
            return "/error";
        }
    }
}

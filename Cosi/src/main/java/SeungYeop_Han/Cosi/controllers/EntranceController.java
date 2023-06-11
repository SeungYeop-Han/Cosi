package SeungYeop_Han.Cosi.controllers;

import SeungYeop_Han.Cosi.DTOs.UsernamePassword;
import SeungYeop_Han.Cosi.services.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EntranceController {

    private static MemberService memberService;

    @Autowired
    public EntranceController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 시작 페이지 요청에 응답합니다.
     * @return : 시작 페이지
     */
    @RequestMapping(value = {"/", "/home"})
    public String welcome() {
        return "/home";
    }

    /**
     * 로그인 요청에 응답합니다.
     * @param error: 에러 여부(true or false)
     * @param exception: 예외 메시지
     * @param model
     * @return
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "exception", required = false) String exception,
                        Model model) {
        model.addAttribute("error", error);
        model.addAttribute("exception", exception);
        return "/login";
    }
}

package SeungYeop_Han.Cosi.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    /**
     * 에러페이지를 반환합니다.
     * @return
     */
    @GetMapping("/error")
    public String error() {
        return "/error";
    }
}

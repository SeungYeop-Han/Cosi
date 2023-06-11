package SeungYeop_Han.Cosi.services;

import SeungYeop_Han.Cosi.DTOs.RegistrationRequest;
import SeungYeop_Han.Cosi.domains.ConfirmationToken;
import SeungYeop_Han.Cosi.domains.Member;
import SeungYeop_Han.Cosi.domains.UserRole;
import SeungYeop_Han.Cosi.repositories.ConfirmationTokenRepository;
import SeungYeop_Han.Cosi.repositories.MemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RegistrationService {

    ///// 의존성 주입 /////
    private final MemberService memberService;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailValidator emailValidator;
    private final EmailSender emailSender;
    private final MemberRepository memberRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;


    /**
     * 주어진 계정 등록 폼(request)을 이용하여 계정을 생성 및 등록합니다.
     * @param request RegistrationRequest: 계정 등록 폼
     * @return 만약 계정 등록에 성공한 경우 성공 메시지를 반환합니다. 그 외의 경우 예외가 발생하게 됩니다.
     */
    public String register(RegistrationRequest request) {

        //이메일 평가
        String email = request.getEmail();
        if ( ! memberService.isValidEmail(email)) {
            throw new IllegalStateException(memberService.evaluateEmail(email));
        }

        //패스워드 형식 검사
        if ( ! memberService.testPassword(request.getPassword())) {
            throw new RuntimeException("회원가입 실패: 잘못된 패스워드 형식.\n" +
                    "비밀번호는 8 ~ 20자 길이, 최소 하나의 영문자, 숫자, 그리고 특수문자로 이루어져야 합니다.");
        }

        //새 회원 생성
        Member member = new Member(
                request.getEmail(),
                request.getPassword(),
                request.getName(),
                UserRole.ROLE_USER
        );

        //패스워드 암호화 및 설정
        memberService.encodePassword(member);

        //회원에게 최초 자본 제공
        memberService.initializeSeed(member);

        //회원 영속화
        memberRepository.save(member);

        //인증 토큰 생성
        ConfirmationToken confirmationToken = confirmationTokenService.make(member, 15);
        confirmationTokenService.save(confirmationToken);

        //인증 메일 전송
        String token = confirmationToken.getToken();
        String link = "http://localhost:8080/registration/confirmed?token=" + token;

        try {
            emailSender.send(
                    request.getEmail(),
                    buildEmail(request.getName(), link)
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());

            memberRepository.findByEmail(request.getEmail())
                    .ifPresent(m -> {
                        confirmationTokenRepository.deleteConfirmationTokensByMemberId(m.getId());
                        memberRepository.deleteById(m.getId());
                    });

            throw e;
        }

        return "성공적으로 계정이 등록되었습니다.";
    }

    /**
     * 인증 메일을 내용을 생성 및 반환합니다.
     * @param name Stirng: 회원의 이름
     * @param link String: 인증 링크
     * @return html 형식의 이메일 내용 문자열
     */
    private String buildEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">계정 활성화를 위한 인증 이메일</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">안녕하세요 " + name + "님,</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> 서비스에 가입해주셔서 감사드립니다. 아래 링크를 클릭해서 계정을 활성화한 다음 본 서비스를 이용하실 수 있습니다: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">인증 링크</a> </p></blockquote>\n 해당 링크는 15분 동안만 유효합니다. <p>환영합니다:D</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }
}

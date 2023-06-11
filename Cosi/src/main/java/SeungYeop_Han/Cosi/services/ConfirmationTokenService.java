package SeungYeop_Han.Cosi.services;

import SeungYeop_Han.Cosi.domains.ConfirmationToken;
import SeungYeop_Han.Cosi.domains.Member;
import SeungYeop_Han.Cosi.repositories.ConfirmationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConfirmationTokenService {
    ///// 의존성 주입 /////
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final MemberService memberService;

    @Autowired
    public ConfirmationTokenService(ConfirmationTokenRepository confirmationTokenRepository,
                                    MemberService memberService) {
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.memberService = memberService;
    }

    ///// 사용자 정의 메소드 /////

    /**
     * 주어진 member에 대해 validTime(분) 만큼 유효한 인증 토큰 엔티티 하나를 생성 및 반환합니다.
     * @param member Member: 인증을 요청한 회원
     * @param validTime int 유효 시간 (분)
     * @return 생성된 인증 토큰 엔티티
     */
    public ConfirmationToken make(Member member, int validTime) {
        if(validTime < 1){
            throw new RuntimeException("인증 시간은 1 분 이상이어야 합니다.");
        }

        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(validTime),
                member
        );

        return confirmationToken;
    }

    /**
     * 주어진 인증토큰을 영속화합니다.
     * @param confirmationToken ConfirmationToken: 영속화 할 인증 토큰
     */
    public void save(ConfirmationToken confirmationToken) {
        confirmationTokenRepository.save(confirmationToken);
    }

    /**
     * 주어진 문자열 토큰과 일치하는 인증 토큰을 찾아서 반환합니다.
     * @param token
     * @return Optional<ConfirmationToken> 주어진 문자열 토큰과 일치하는 인증 토큰
     */
    public Optional<ConfirmationToken> getByToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    /**
     * 주어진 문자열 토큰을 이용하여 찾은 인증토큰의 확인 시간을 요청 당시 시간으로 초기화합니다.
     * @param token String: 문자열 토큰
     * @return
     */
    public int setConfirmedAt(String token) {
        return confirmationTokenRepository.updateConfirmedAt(token, LocalDateTime.now());
    }

    /**
     * 인증 토큰을 인증처리합니다. 인증에 실패한 경우 예외를 발생시킵니다.
     * @param token String: 토큰 문자열
     * @return String: 인증 성공 메시지
     */
    @Transactional
    public String confirm(String token) {
        ConfirmationToken confirmationToken = this
                .getByToken(token)
                .orElseThrow(
                        () -> new IllegalStateException("인증 토큰을 찾을 수 없습니다.")
                );

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("이미 인증이 완료된 이메일입니다.");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiredAt();
        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("토큰이 만료되었습니다.");
        }

        setConfirmedAt(token);
        memberService.enableMember(confirmationToken.getMember().getEmail());

        return "인증이 정상적으로 완료되었습니다.";
    }
}

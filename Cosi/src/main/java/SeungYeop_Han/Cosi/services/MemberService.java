package SeungYeop_Han.Cosi.services;

import SeungYeop_Han.Cosi.domains.Member;
import SeungYeop_Han.Cosi.repositories.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MemberService implements UserDetailsService {
    ///// 속성 /////
    //에러 메시지
    private final static String MEMBER_NOT_FOUND_MSG =
            "주어진 계정 %s 은/는 미등록된 계정입니다.";


    ///// 의존성 주입 /////
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailValidator emailValidator;

    public MemberService(BCryptPasswordEncoder bCryptPasswordEncoder,
                         MemberRepository memberRepository,
                         EmailValidator emailValidator) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.memberRepository = memberRepository;
        this.emailValidator = emailValidator;
    }

    ///// 메소드 재정의: UserDetailsService /////
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return memberRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                String.format(MEMBER_NOT_FOUND_MSG, email)));
    }

    ///// 사용자 정의 메소드 /////
    /**
     * 제시된 이메일이 중복되었는지, 혹은 유효한지 등을 검사하고 적절한 상태 메시지를 반환합니다.
     * @param email String: 확인 하고자 하는 이메일
     * @return String: 이메일 중복 혹은 형식의 유효성에 대한 메시지
     */
    @Transactional
    public String evaluateEmail(String email) {

        if(emailValidator.test(email)){

            Optional<Member> member = memberRepository.findByEmail(email);
            if (member.isPresent()) {
                return "이미 사용 중인 이메일입니다.";
            }
            else{
                return "사용 가능한 이메일입니다.";
            }

        }
        else{
            return "잘못된 이메일 형식입니다.";
        }
    }

    /**
     * 주어진 이메일 형식이 유효하고 중복되지 않은 경우에만 true를 반환하고, 그렇지 않으면 false를 반환합니다.
     * @param email String: 확인하고자 하는 이메일 주소
     * @return boolean: 이메일 이용 가능 여부
     */
    public boolean isValidEmail(String email){
        boolean isEmailValid = emailValidator.test(email);
        boolean isEmailDuplicated = memberRepository
                .findByEmail(email)
                .isPresent();

        return isEmailValid && !isEmailDuplicated;
    }

    /**
     * 비밀번호 형식을 검사합니다.
     * @param rawPassword
     * @return boolean - 만약 유효한 형식이면 true를, 그렇지 않으면 false를 반환합니다.
     */
    public boolean testPassword(String rawPassword) {
        //비밀번호 검증을 위한 정규 표현식
        //최소 8 ~ 최대 20자, 최소 하나의 영문자, 숫자, 그리고 특수문자를 포함해야합니다.
        final Pattern VALID_PASSWORD_REGEX =
                Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,20}$");

        //정규 표현식을 이용하여 이메일 형식을 검사합니다.
        Matcher matcher = VALID_PASSWORD_REGEX.matcher(rawPassword);
        return matcher.find();
    }

    /**
     * 주어진 member의 seed 를 주어진 시드의 양 만큼으로 초기화합니다.
     * @param member Member: 시드를 초기화할 Member 엔티티
     * @param seedAmount Double: 초기화할 시드의 양
     */
    public void initializeSeed(Member member, Double seedAmount){

        if(seedAmount <= 0.0){
            throw new RuntimeException("초기화 할 시드의 0 이상이어야 합니다.");
        }

        member.setSeed(seedAmount);
    }
    /**
     * 주어진 member의 seed 를 기본 시드 양(30,000,000.0)으로 초기화합니다.
     * @param member Member: 시드를 초기화할 Member 엔티티
     */
    public void initializeSeed(Member member){
        initializeSeed(member, 30_000_000.0);
    }

    /**
     * 주어진 member의 패스워드를 주어진 password를 암호화한 것으로 설정하고 암호화된 패스워드를 반환합니다.
     * @param member Member: 패스워드를 암호화할 Member 엔티티
     * @param password String: 평문 패스워드 문자열
     * @return String: 암호화된 패스워드 문자열
     */
    public String encodePassword(Member member, String password) {
        String encodedPassword = bCryptPasswordEncoder
                .encode(password);
        member.setPassword(encodedPassword);

        return encodedPassword;
    }

    /**
     * 주어진 member의 패스워드를 기 등록된 패스워드를 암호화한 것으로 설정하고 암호화된 패스워드를 반환합니다.
     * @param member Member: 패스워드를 암호화할 Member 엔티티
     * @return String: 암호화된 패스워드 문자열
     */
    public String encodePassword(Member member) {
        String encodedPassword = bCryptPasswordEncoder
                .encode(member.getPassword());
        member.setPassword(encodedPassword);

        return encodedPassword;
    }

    /**
     * 주어진 이메일에 해당하는 계정을 활성화합니다.
     * @param email String: 이메일 주소(키)
     * @return ?
     */
    public int enableMember(String email) {
        return memberRepository.enableMember(email);
    }
}

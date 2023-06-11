package SeungYeop_Han.Cosi.services;

import org.springframework.stereotype.Service;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailValidator implements Predicate<String> {

    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);


    /**
     * 이메일 형식을 검사합니다.
     * @param email the input argument
     * @return boolean - 만약 유효한 형식이면 true를, 그렇지 않으면 false를 반환합니다.
     */
    public boolean test(String email) {
        //정규 표현식을 이용하여 이메일 형식을 검사합니다.
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.find();
    }
}

package SeungYeop_Han.Cosi.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService implements EmailSender {

    ///// 속성 /////
    private final static Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    ///// 메소드 재정의 /////

    /**
     * 목적지 이메일 주소 to 로 emailContents를 전송합니다.
     * @param to String: 목적지 이메일 주소
     * @param emailContents String: html 문자열로 표현된 이메일 내용
     */
    @Override
    @Async
    public void send(String to, String emailContents) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setText(emailContents, true);
            helper.setTo(to);
            helper.setSubject("이메일 인증");

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            LOGGER.error("이메일 전송 실패", e);
            throw new IllegalStateException("이메일 전송 실패");
        }
    }
}

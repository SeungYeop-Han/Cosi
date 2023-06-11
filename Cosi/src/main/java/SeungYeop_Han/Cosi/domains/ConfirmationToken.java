package SeungYeop_Han.Cosi.domains;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Table(name = "confirmation_tokens")
@Entity(name = "ConfirmationToken")
@Getter
@Setter
@NoArgsConstructor
public class ConfirmationToken {
    ///// 속성 /////
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @NonNull
    @Column(name = "token")
    private String token;

    @NonNull
    @CreatedDate
    @Column(
            name = "created_at",
            columnDefinition = "DATETIME"
    )
    private LocalDateTime createdAt;

    @NonNull
    @Column(
            name = "expired_at",
            columnDefinition = "DATETIME"
    )
    private LocalDateTime expiredAt;

    @Column(
            name = "confirmed_at",
            columnDefinition = "DATETIME"
    )
    private LocalDateTime confirmedAt;

    /////// 참조 속성 /////
    @NonNull
    @ManyToOne
    @JoinColumn(
            name = "member_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "confirmation_tokens_member_id_fk")
    )
    private Member member;

    ///// 생성자 /////
    public ConfirmationToken(String token,
                             LocalDateTime createdAt,
                             LocalDateTime expiredAt,
                             Member member) {
        this.token = token;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
        this.member = member;
    }
}

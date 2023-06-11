package SeungYeop_Han.Cosi.domains;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class WalletId implements Serializable {
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "coin_id")
    private Long coinId;

    public WalletId(){}
}

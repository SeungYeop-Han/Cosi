package SeungYeop_Han.Cosi.domains;

import jakarta.persistence.*;
import lombok.*;

@Table(
        name = "coins",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "coins_code_uk",
                        columnNames = "code"
                )
        }
)
@Entity(name = "Coin")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Coin {
    ///// 속성 /////
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @Column(
            name = "code",
            length = 10
    )
    @NonNull
    private String code;

    @Column(
            name = "name",
            length = 30
    )
    @NonNull
    private String name;

    ///// 생성자 /////
    public Coin(String code, String name){
        this.code = code;
        this.name = name;
    }

    ///// 메소드 재정의: toString /////
    @Override
    public String toString(){
        return "Coin{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

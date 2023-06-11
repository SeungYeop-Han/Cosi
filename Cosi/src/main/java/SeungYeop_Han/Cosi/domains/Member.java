package SeungYeop_Han.Cosi.domains;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Table(
        name = "members",                           //테이블 이름
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "members_email_uk",  //제약조건 이름
                        columnNames = "email"       //컬럼 이름
                )
        }
)
@Entity(name = "Member")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Member implements UserDetails {
    ///// 속성 /////
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @Column(
            name = "email",
            length = 320
    )
    @NonNull
    private String email;

    @Column(name = "password")
    @NonNull
    private String password;

    @Column(name = "name")
    @NonNull
    private String name;

    @Column(name = "user_role")
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column(name = "locked")
    @NonNull
    private Boolean locked = false;

    @Column(name = "enabled")
    @NonNull
    private Boolean enabled = false;

    @Column(
            name = "seed",
            columnDefinition = "DECIMAL(16, 4) CHECK(seed >= 0.0)"
    )
    @NonNull
    private Double seed;    //원화 시드를 의미함

    ///// 역 방향 참조 속성 /////
    @OneToMany(
            mappedBy = "member",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private final List<Wallet> wallets = new ArrayList<>();

    @OneToMany(
            mappedBy = "member",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private final List<Order> orders = new ArrayList<>();

    ///// 생성자 /////
    public Member(String email,
                  String password,
                  String name,
                  UserRole userRole) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.userRole = userRole;
    }

    ///// 메소드 재정의: toString /////
    @Override
    public String toString(){
        return "Member{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    ///// 메소드 재정의: implements UserDetails /////
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return authorities;
    }

    @Override
    public String getPassword() { return this.password; }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    ///// 사용자 정의 메소드 /////
    /**
     * 멤버(this)에게 매개변수에 주어진 지갑을 줍니다.
     * 만약 지갑의 소유자와 멤버(this)가 다른 사람이거나, 주어진 지갑과 같은 종류의 코인 지갑을 이미 멤버(this)가 가지고 있는 경우 예외를 던집니다.
     * @param wallet Wallet: 줄 지갑
     */
    public void addWallet(Wallet wallet) {
        this.wallets.forEach(w -> {
            if(!w.getWalletId().getMemberId()
                    .equals(this.getId())){
                throw new RuntimeException("지갑 소유 멤버와 수령할 멤버가 일치하지 않습니다.");
            }

            if(w.getWalletId().getCoinId()
                    .equals(wallet.getCoin().getId())){
                throw new RuntimeException(
                        String.format(
                                "멤버 %s 은/는 이미 %s 지갑을/를 가지고 있습니다.", this.toString(), wallet.getCoin().getCode()
                        )
                );
            }
        });

        this.wallets.add(wallet);
    }

    /**
     * 멤버(this)에서 매개변수에 주어진 지갑을 삭제합니다.
     * 만약 멤버(this)가 해당 지갑을 가지고 있지 않은 경우 예외를 발생시킵니다.
     * @param wallet
     */
    public void removeWallet(Wallet wallet) {
        if(this.wallets.contains(wallet)){
            this.wallets.remove(wallet);
        }
        else{
            throw new RuntimeException(
                    String.format(
                            "멤버 %s 은/는 지갑 %s 을/를 가지고 있지 않습니다.", this.toString(), wallet
                    )
            );
        }
    }

    /**
     * 주어진 주문을 Member 엔티티에 추가합니다.
     * 만약 주문자와 멤버(this)가 일치하지 않거나 이미 처리된 주문인경우 예외를 던집니다.
     * @param order Order: 추가할 멤버
     */
    public void addOrder(Order order) {
        Long ordererId = order.getMember().getId();
        if(!ordererId.equals(this.id)){
                throw new RuntimeException("주문에 명시된 주문자가 일치하지 않습니다.");
        }

        if(this.orders.contains(order)){
            throw new RuntimeException(String.format("주문 %s 은/는 이미 멤버 %s 에 대해 처리되어 있습니다.", this, order));
        }
        this.orders.add(order);
    }

    /**
     * 주어진 주문을 this Member 엔티티에서 삭제합니다.
     * 만약 해당 주문이 this Member 엔티티에 존재하지 않는 경우 예외를 던집니다.
     * @param order
     */
    public void removeOrder(Order order) {
        if(this.orders.contains(order)){
            this.orders.remove(order);
        }
        else{
            throw new RuntimeException(
                    String.format("주문자: %s 에 대한 주문: %s 의 기록이 없습니다.", this, order)
            );
        }
    }

    /**
     * 주어진 양 만큼 시드를 소비합니다.
     * @param price Double: 소비할 시드의 양
     * @throws RuntimeException 만약 주어진 시드의 양이 음수이거나, 보유 시드보다 클 경우 예외를 발생시킵니다.
     */
    public void spend(Double price) throws RuntimeException{
        if(price < 0) {
            throw new RuntimeException("차감(소비)할 금액은 양수여야 합니다.");
        }

        if(price > this.seed) {
            throw new RuntimeException("차감(소비) 금액이 보유 시드보다 많습니다");
        }

        this.seed -= price;
    }

    /**
     * 주어진 양 만큼 시드를 얻습니다.
     * @param seedAmountToEarn Double: 얻을 시드의 양
     * @throws RuntimeException 만약 주어진 시드의 양이 음수인 경우 예외를 발생시킵니다.
     */
    public void earn(Double seedAmountToEarn) throws RuntimeException{
        if(seedAmountToEarn < 0){
            throw new RuntimeException("지급 금액은 양수여야 합니다.");
        }

        this.seed += seedAmountToEarn;
    }
}

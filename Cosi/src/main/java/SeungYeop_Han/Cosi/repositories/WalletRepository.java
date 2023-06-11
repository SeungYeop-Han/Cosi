package SeungYeop_Han.Cosi.repositories;

import SeungYeop_Han.Cosi.domains.Wallet;
import SeungYeop_Han.Cosi.domains.WalletId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, WalletId> {

    @Query("SELECT w FROM Wallet w WHERE w.member.id = :memberId")
    public List<Wallet> findWalletsByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT w FROM Wallet w WHERE w.member.id = :memberId AND w.coin.id = :coinId")
    public Optional<Wallet> findWalletByMemberIdAndCoinId(@Param("memberId") Long memberId, @Param("coinId") Long coinId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Wallet w WHERE w.member.id = :memberId")
    public void deleteWalletsByMemberId(@Param("memberId") Long memberId);
}

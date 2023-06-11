package SeungYeop_Han.Cosi.repositories;

import SeungYeop_Han.Cosi.domains.ConfirmationToken;
import SeungYeop_Han.Cosi.domains.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {
    public Optional<ConfirmationToken> findByToken(String token);

    @Query("SELECT ct.member FROM ConfirmationToken ct WHERE ct.token = :token")
    public Optional<Member> findMemberByToken(@Param("token") String token);

    @Transactional
    @Modifying
    @Query("UPDATE ConfirmationToken c SET c.confirmedAt = :confirmedAt WHERE c.token = :token")
    public int updateConfirmedAt(@Param("token") String token, @Param("confirmedAt") LocalDateTime confirmedAt);

    @Transactional
    @Modifying
    @Query("DELETE FROM ConfirmationToken c WHERE c.member.id = :id")
    public int deleteConfirmationTokensByMemberId(@Param("id") Long id);
}

package SeungYeop_Han.Cosi.repositories;

import SeungYeop_Han.Cosi.domains.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    @Transactional
    @Modifying
    @Query("UPDATE Member m SET m.enabled = TRUE WHERE m.email = :email")
    public int enableMember(@Param("email") String email);

    @Query("SELECT m FROM Member m WHERE m.email = :email")
    public Optional<Member> findByEmail(@Param("email") String email);
}

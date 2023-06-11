package SeungYeop_Han.Cosi.repositories;

import SeungYeop_Han.Cosi.domains.Coin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface CoinRepository extends JpaRepository<Coin, Long> {
    @Query
    public Optional<Coin> findCoinByCode(String code);
}

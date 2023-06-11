package SeungYeop_Han.Cosi.repositories;

import SeungYeop_Han.Cosi.domains.FilledOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface FilledOrderRepository extends JpaRepository<FilledOrder, Long> {
    @Transactional
    @Modifying
    @Query("DELETE FROM FilledOrder fo WHERE fo.id = :orderId")
    public void deleteFilledOrdersByOrderId(@Param("orderId") Long orderId);
}

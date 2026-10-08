package tn.esprit.wafik_lahbib_4cce10.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.wafik_lahbib_4cce10.domain.Paiement;

public interface IPaiementRepository extends JpaRepository<Paiement, Long> {
}

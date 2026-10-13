package tn.esprit.wafik_lahbib_4cce10.service;

import java.util.List;
import tn.esprit.wafik_lahbib_4cce10.domain.Paiement;

public interface IPaiementService {

    Paiement findById(Long id);

    List<Paiement> findAll();
}

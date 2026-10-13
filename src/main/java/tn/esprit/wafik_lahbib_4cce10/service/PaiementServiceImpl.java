package tn.esprit.wafik_lahbib_4cce10.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.wafik_lahbib_4cce10.domain.Paiement;
import tn.esprit.wafik_lahbib_4cce10.exception.ResourceNotFoundException;
import tn.esprit.wafik_lahbib_4cce10.repository.IPaiementRepository;

// lecture seule : un paiement se crée, se modifie et se supprime via son contrat
@Service
@RequiredArgsConstructor
public class PaiementServiceImpl implements IPaiementService {

    private final IPaiementRepository paiementRepository;

    @Override
    public Paiement findById(Long id) {
        return paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", id));
    }

    @Override
    public List<Paiement> findAll() {
        return paiementRepository.findAll();
    }
}

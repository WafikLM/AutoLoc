package tn.esprit.wafik_lahbib_4cce10.service;

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.wafik_lahbib_4cce10.domain.Contrat;
import tn.esprit.wafik_lahbib_4cce10.domain.Paiement;
import tn.esprit.wafik_lahbib_4cce10.exception.ResourceNotFoundException;
import tn.esprit.wafik_lahbib_4cce10.repository.IContratRepository;

@Service
@RequiredArgsConstructor
public class ContratServiceImpl implements IContratService {

    private final IContratRepository contratRepository;

    @Override
    public Contrat create(Contrat contrat) {
        if (contrat.getIdContrat() != null) {
            throw new IllegalArgumentException(
                    "Un nouveau contrat ne doit pas avoir d'identifiant");
        }
        verifier(contrat);
        return contratRepository.save(contrat);
    }

    @Override
    public Contrat findById(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", id));
    }

    @Override
    public List<Contrat> findAll() {
        return contratRepository.findAll();
    }

    @Override
    public Contrat update(Long id, Contrat contrat) {
        verifier(contrat);
        Contrat existant = findById(id);
        existant.setDateSignature(contrat.getDateSignature());
        existant.setMontantTotal(contrat.getMontantTotal());
        existant.setValide(contrat.isValide());
        // on ne touche ni à l'identifiant ni à la collection de paiements
        return contratRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!contratRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contrat", id);
        }
        contratRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Contrat addWithPayments(Contrat contrat) {
        if (contrat.getIdContrat() != null) {
            throw new IllegalArgumentException(
                    "Un nouveau contrat ne doit pas avoir d'identifiant");
        }
        verifier(contrat);
        if (contrat.getPaiements() == null || contrat.getPaiements().isEmpty()) {
            throw new IllegalArgumentException("Le contrat doit contenir au moins un paiement");
        }
        BigDecimal totalPaiements = BigDecimal.ZERO;
        for (Paiement paiement : contrat.getPaiements()) {
            if (paiement.getIdPaiement() != null) {
                throw new IllegalArgumentException(
                        "Un nouveau paiement ne doit pas avoir d'identifiant");
            }
            if (paiement.getMontant() == null
                    || paiement.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant d'un paiement doit être positif");
            }
            // lien côté propriétaire : sans lui, la clé étrangère du paiement serait nulle
            paiement.setContrat(contrat);
            totalPaiements = totalPaiements.add(paiement.getMontant());
        }
        if (contrat.getMontantTotal() != null
                && totalPaiements.compareTo(contrat.getMontantTotal()) > 0) {
            throw new IllegalArgumentException(
                    "La somme des paiements dépasse le montant total du contrat");
        }
        // un seul save : les paiements sont insérés par la cascade (CascadeType.ALL)
        return contratRepository.save(contrat);
    }

    // règle de gestion
    private void verifier(Contrat contrat) {
        if (contrat.getMontantTotal() != null
                && contrat.getMontantTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le montant total ne peut pas être négatif");
        }
    }
}

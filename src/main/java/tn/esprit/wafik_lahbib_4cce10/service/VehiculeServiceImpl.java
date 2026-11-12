package tn.esprit.wafik_lahbib_4cce10.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.wafik_lahbib_4cce10.domain.Agence;
import tn.esprit.wafik_lahbib_4cce10.domain.Equipement;
import tn.esprit.wafik_lahbib_4cce10.domain.Vehicule;
import tn.esprit.wafik_lahbib_4cce10.exception.ResourceNotFoundException;
import tn.esprit.wafik_lahbib_4cce10.repository.IAgenceRepository;
import tn.esprit.wafik_lahbib_4cce10.repository.IEquipementRepository;
import tn.esprit.wafik_lahbib_4cce10.repository.IVehiculeRepository;

@Service
@RequiredArgsConstructor
public class VehiculeServiceImpl implements IVehiculeService {

    private final IVehiculeRepository vehiculeRepository;
    private final IAgenceRepository agenceRepository;
    private final IEquipementRepository equipementRepository;

    @Override
    public Vehicule create(Vehicule vehicule) {
        if (vehicule.getIdVehicule() != null) {
            throw new IllegalArgumentException(
                    "Un nouvel objet Vehicule ne doit pas avoir d'identifiant");
        }
        verifier(vehicule);
        return vehiculeRepository.save(vehicule);
    }

    @Override
    public Vehicule findById(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", id));
    }

    @Override
    public List<Vehicule> findAll() {
        return vehiculeRepository.findAll();
    }

    @Override
    public Vehicule update(Long id, Vehicule vehicule) {
        verifier(vehicule);
        Vehicule existant = findById(id);
        existant.setImmatriculation(vehicule.getImmatriculation());
        existant.setMarque(vehicule.getMarque());
        existant.setModele(vehicule.getModele());
        existant.setCategorie(vehicule.getCategorie());
        existant.setTarifJournalier(vehicule.getTarifJournalier());
        existant.setStatut(vehicule.getStatut());
        return vehiculeRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!vehiculeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicule", id);
        }
        vehiculeRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Vehicule addAndAssignAgence(Vehicule vehicule, Long idAgence) {
        if (vehicule.getIdVehicule() != null) {
            throw new IllegalArgumentException(
                    "Un nouvel objet Vehicule ne doit pas avoir d'identifiant");
        }
        verifier(vehicule);
        Agence agence = trouverAgence(idAgence);
        vehicule.setAgence(agence);

        // nouveaux équipements (sans id) : créés par la cascade PERSIST
        // équipements existants (avec id) : on reprend ceux de la base
        Set<Equipement> equipements = new HashSet<>();
        if (vehicule.getEquipements() != null) {
            for (Equipement e : vehicule.getEquipements()) {
                if (e.getIdEquipement() == null) {
                    if (e.getLibelle() == null || e.getLibelle().isBlank()) {
                        throw new IllegalArgumentException("Le libellé de l'équipement est obligatoire");
                    }
                    equipements.add(e);
                } else {
                    equipements.add(trouverEquipement(e.getIdEquipement()));
                }
            }
        }
        vehicule.setEquipements(equipements);
        return vehiculeRepository.save(vehicule);
    }

    @Override
    @Transactional
    public Vehicule assignAgence(Long idVehicule, Long idAgence) {
        Vehicule vehicule = findById(idVehicule);
        Agence agence = trouverAgence(idAgence);
        vehicule.setAgence(agence);
        return vehiculeRepository.save(vehicule);
    }

    @Override
    @Transactional
    public Vehicule assignEquipement(Long idVehicule, Long idEquipement) {
        Vehicule vehicule = findById(idVehicule);
        Equipement equipement = trouverEquipement(idEquipement);
        boolean dejaAffecte = vehicule.getEquipements().stream()
                .anyMatch(e -> e.getIdEquipement().equals(idEquipement));
        if (dejaAffecte) {
            throw new IllegalArgumentException("Cet équipement est déjà affecté à ce véhicule");
        }
        vehicule.getEquipements().add(equipement);
        return vehiculeRepository.save(vehicule);
    }

    @Override
    @Transactional
    public Vehicule unassignEquipement(Long idVehicule, Long idEquipement) {
        Vehicule vehicule = findById(idVehicule);
        trouverEquipement(idEquipement);
        boolean retire = vehicule.getEquipements()
                .removeIf(e -> e.getIdEquipement().equals(idEquipement));
        if (!retire) {
            throw new IllegalArgumentException("Cet équipement n'est pas affecté à ce véhicule");
        }
        return vehiculeRepository.save(vehicule);
    }

    private Agence trouverAgence(Long idAgence) {
        return agenceRepository.findById(idAgence)
                .orElseThrow(() -> new ResourceNotFoundException("Agence", idAgence));
    }

    private Equipement trouverEquipement(Long idEquipement) {
        return equipementRepository.findById(idEquipement)
                .orElseThrow(() -> new ResourceNotFoundException("Equipement", idEquipement));
    }

    // règle de gestion
    private void verifier(Vehicule vehicule) {
        if (vehicule.getTarifJournalier() != null
                && vehicule.getTarifJournalier().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le tarif journalier ne peut pas être négatif");
        }
    }
}

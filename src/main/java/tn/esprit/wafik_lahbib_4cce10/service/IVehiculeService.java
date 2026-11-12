package tn.esprit.wafik_lahbib_4cce10.service;

import java.util.List;
import tn.esprit.wafik_lahbib_4cce10.domain.Vehicule;

public interface IVehiculeService {

    Vehicule create(Vehicule vehicule);

    Vehicule findById(Long id);

    List<Vehicule> findAll();

    Vehicule update(Long id, Vehicule vehicule);

    void deleteById(Long id);

    // ajoute un véhicule avec ses équipements (cascade) et l'affecte à une agence existante
    Vehicule addAndAssignAgence(Vehicule vehicule, Long idAgence);

    // affecte un véhicule existant à une agence existante
    Vehicule assignAgence(Long idVehicule, Long idAgence);

    // ajoute un équipement existant à un véhicule existant
    Vehicule assignEquipement(Long idVehicule, Long idEquipement);

    // retire un équipement d'un véhicule (l'équipement reste en base)
    Vehicule unassignEquipement(Long idVehicule, Long idEquipement);
}

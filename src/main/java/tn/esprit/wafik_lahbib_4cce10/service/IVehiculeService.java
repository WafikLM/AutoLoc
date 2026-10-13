package tn.esprit.wafik_lahbib_4cce10.service;

import java.util.List;
import tn.esprit.wafik_lahbib_4cce10.domain.Vehicule;

public interface IVehiculeService {

    Vehicule create(Vehicule vehicule);

    Vehicule findById(Long id);

    List<Vehicule> findAll();

    Vehicule update(Long id, Vehicule vehicule);

    void deleteById(Long id);
}
